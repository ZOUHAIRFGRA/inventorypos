package com.fouiguira.pos.inventorypos.services.impl;

import com.fouiguira.pos.inventorypos.entities.Invoice;
import com.fouiguira.pos.inventorypos.entities.Sale;
import com.fouiguira.pos.inventorypos.entities.User;
import com.fouiguira.pos.inventorypos.repositories.InvoiceRepository;
import com.fouiguira.pos.inventorypos.services.interfaces.InvoiceService;
import com.fouiguira.pos.inventorypos.services.interfaces.SalesService;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Service
public class InvoiceServiceImpl implements InvoiceService {

   private final InvoiceRepository invoiceRepository;
    private SalesService salesService;

    public InvoiceServiceImpl(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    @Autowired
    @Lazy
    public void setSalesService(SalesService salesService) {
        this.salesService = salesService;
    }

    @Override
    public Invoice createInvoiceFromSale(Long saleId) {
        Sale sale = salesService.getSaleById(saleId);
        if (sale == null) {
            throw new RuntimeException("Sale not found with id: " + saleId);
        }
        Invoice invoice = new Invoice();
        invoice.setSale(sale);
        invoice.setTotalAmount(sale.getTotalPrice());
        invoice.setClientName(sale.getClientName());
        invoice.setCashier(sale.getCashier());
        invoice.setTimestamp(new Date());
        invoice.setStatus(Invoice.InvoiceStatus.PENDING);
        return invoiceRepository.save(invoice);
    }

    @Override
    public Invoice getInvoiceById(Long invoiceId) {
        return invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found with id: " + invoiceId));
    }

    @Override
    public List<Invoice> getInvoicesByDate(LocalDate date) {
        if (date == null) {
            return invoiceRepository.findAll();
        }
        LocalDate start = date.atStartOfDay().toLocalDate();
        LocalDate end = date.plusDays(1).atStartOfDay().toLocalDate();
        return invoiceRepository.findByTimestampBetween(start, end);
    }

    @Override
    public List<Invoice> getPendingInvoices() {
        return invoiceRepository.findByStatus(Invoice.InvoiceStatus.PENDING);
    }

    @Override
    public int getPendingInvoicesCount() {
        return getPendingInvoices().size();
    }

    @Override
    public void updateInvoiceStatus(Long invoiceId, Invoice.InvoiceStatus status) {
        Invoice invoice = getInvoiceById(invoiceId);
        invoice.setStatus(status);
        invoice.setUpdatedAt(new Date());
        invoiceRepository.save(invoice);
    }

    @Override
    public List<Invoice> getInvoicesByClientName(String clientName) {
        return invoiceRepository.findByClientNameContainingIgnoreCase(clientName);
    }

    @Override
    public List<Invoice> getInvoicesByCashier(User cashier) {
        return invoiceRepository.findByCashier(cashier);
    }

    @Override
    public Invoice getInvoiceBySaleId(Long saleId) {
        List<Invoice> invoices = invoiceRepository.findBySaleId(saleId);
        return invoices.isEmpty() ? null : invoices.get(0); // Return first invoice or null
    }

    @Override
    public void generateInvoicePdf(Invoice invoice) {
        try {
            // Ensure the products collection is initialized
            Sale sale = invoice.getSale();
            sale.getProducts().size(); // This will initialize the products collection

            String fileName = "invoice_" + invoice.getId() + "_" + System.currentTimeMillis() + ".pdf";
            PdfWriter writer = new PdfWriter(fileName);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("Inventory POS - Invoice #" + invoice.getId()));
            document.add(new Paragraph("Date: " + invoice.getTimestamp()));
            document.add(new Paragraph("Client: " + invoice.getClientName()));
            document.add(new Paragraph("Cashier: " + invoice.getCashier().getUsername()));
            document.add(new Paragraph("Status: " + invoice.getStatus()));
            document.add(new Paragraph("Total Amount: $" + String.format("%.2f", invoice.getTotalAmount())));

            Table table = new Table(4); // Added column for unit price
            table.addCell(new Cell().add(new Paragraph("Product")));
            table.addCell(new Cell().add(new Paragraph("Unit Price")));
            table.addCell(new Cell().add(new Paragraph("Quantity")));
            table.addCell(new Cell().add(new Paragraph("Total Price")));
            sale.getProducts().forEach(sp -> {
                table.addCell(new Cell().add(new Paragraph(sp.getProduct().getName())));
                table.addCell(new Cell().add(new Paragraph("$" + String.format("%.2f", sp.getProduct().getPrice()))));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(sp.getQuantity()))));
                table.addCell(new Cell().add(new Paragraph("$" + String.format("%.2f", sp.getProduct().getPrice() * sp.getQuantity()))));
            });
            document.add(table);

            document.close();
            System.out.println("Invoice PDF generated: " + new File(fileName).getAbsolutePath());
        } catch (Exception e) {
            System.out.println("Failed to generate invoice PDF: " + e.getMessage());
        }
    }
}