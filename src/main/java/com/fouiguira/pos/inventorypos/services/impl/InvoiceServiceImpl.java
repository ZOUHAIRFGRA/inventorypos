package com.fouiguira.pos.inventorypos.services.impl;

import com.fouiguira.pos.inventorypos.entities.Invoice;
import com.fouiguira.pos.inventorypos.repositories.InvoiceRepository;
import com.fouiguira.pos.inventorypos.services.interfaces.InvoiceService;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;

    public InvoiceServiceImpl(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    @Override
    public Invoice createInvoice(Invoice invoice) {
        return invoiceRepository.save(invoice);
    }

    @Override
    public List<Invoice> getInvoicesByDate(LocalDate date) {
        if (date == null) {
            return invoiceRepository.findAll(); // Return all invoices if date is null
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
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found with id: " + invoiceId));
        invoice.setStatus(status);
        invoice.setUpdatedAt(new Date());
        invoiceRepository.save(invoice);
    }

    @Override
    public void generateInvoicePdf(Invoice invoice) {
        try {
            String fileName = "invoice_" + invoice.getId() + ".pdf";
            PdfWriter writer = new PdfWriter(fileName);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("Invoice #" + invoice.getId()));
            document.add(new Paragraph("Date: " + invoice.getTimestamp()));
            document.add(new Paragraph("Status: " + invoice.getStatus()));
            document.add(new Paragraph("Total Amount: $" + String.format("%.2f", invoice.getTotalAmount())));

            Table table = new Table(3);
            table.addCell(new Cell().add(new Paragraph("Product")));
            table.addCell(new Cell().add(new Paragraph("Quantity")));
            table.addCell(new Cell().add(new Paragraph("Price")));
            invoice.getSale().getProducts().forEach(sp -> {
                table.addCell(new Cell().add(new Paragraph(sp.getProduct().getName())));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(sp.getQuantity()))));
                table.addCell(new Cell().add(new Paragraph("$" + String.format("%.2f", sp.getProduct().getPrice() * sp.getQuantity()))));
            });
            document.add(table);

            document.close();
            System.out.println("Invoice PDF generated: " + new File(fileName).getAbsolutePath());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate invoice PDF: " + e.getMessage());
        }
    }
}