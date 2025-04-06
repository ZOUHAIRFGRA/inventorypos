package com.fouiguira.pos.inventorypos.services.impl;

import com.fouiguira.pos.inventorypos.controllers.SettingsController;
import com.fouiguira.pos.inventorypos.entities.BusinessSettings;
import com.fouiguira.pos.inventorypos.entities.Invoice;
import com.fouiguira.pos.inventorypos.entities.Sale;
import com.fouiguira.pos.inventorypos.entities.SaleProduct;
import com.fouiguira.pos.inventorypos.entities.User;
import com.fouiguira.pos.inventorypos.repositories.InvoiceRepository;
import com.fouiguira.pos.inventorypos.services.interfaces.BusinessSettingsService;
import com.fouiguira.pos.inventorypos.services.interfaces.InvoiceService;
import com.fouiguira.pos.inventorypos.services.interfaces.SalesService;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private SalesService salesService;
    private final BusinessSettingsService settingsService;
    @SuppressWarnings("unused")
    private final SettingsController settingsController;


    public InvoiceServiceImpl(InvoiceRepository invoiceRepository, BusinessSettingsService settingsService, SettingsController settingsController) {
        this.invoiceRepository = invoiceRepository;
        this.settingsService = settingsService;
        this.settingsController = settingsController;
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
    @SuppressWarnings("resource")
    @Override
    @Transactional(readOnly = true) // Ensure settings are fetched in a transaction
    public void generateInvoicePdf(Invoice invoice) {
        BusinessSettings settings = settingsService.getSettings(); // Fetch fresh settings transactionally
        if (settings == null) {
            throw new RuntimeException("Business settings not found");
        }
        System.out.println("Generating PDF with settings: " + settings.getBusinessName() + ", Version: " + settings.getVersion());

        try {
            Sale sale = invoice.getSale();
            if (sale.getProducts() == null || sale.getProducts().isEmpty()) {
                throw new IllegalStateException("Sale products not initialized");
            }

            String fileName = "invoice_" + invoice.getId() + "_" + System.currentTimeMillis() + ".pdf";
            PdfWriter writer = new PdfWriter(fileName);
            PdfDocument pdf = new PdfDocument(writer);
            pdf.setDefaultPageSize(PageSize.A4);
            Document document = new Document(pdf);
            document.setMargins(36, 36, 36, 36);

            Image logo;
            String logoPath = settings.getLogoPath();
            File logoFile = new File(logoPath);
            try {
                if (logoFile.exists() && logoFile.isFile() && logoFile.length() > 0) {
                    logo = new Image(ImageDataFactory.create(logoPath))
                        .scaleToFit(100, 50)
                        .setHorizontalAlignment(HorizontalAlignment.LEFT)
                        .setMarginBottom(5); // Reduced margin between logo and business name
                    System.out.println("Logo loaded from: " + logoPath);
                } else {
                    throw new IOException("Logo file invalid or empty: " + logoPath);
                }
            } catch (Exception e) {
                System.out.println("Failed to load logo from " + logoPath + ": " + e.getMessage());
                File blankImage = new File("config/default_logo.png");
                logo = new Image(ImageDataFactory.create(blankImage.getAbsolutePath()))
                    .scaleToFit(100, 50)
                    .setHorizontalAlignment(HorizontalAlignment.LEFT)
                    .setMarginBottom(5); // Reduced margin between logo and business name
            }

            // Create a table for the header to ensure proper alignment
            Table headerTable = new Table(1).useAllAvailableWidth();
            headerTable.setBorder(Border.NO_BORDER);
            
            Cell logoCell = new Cell().add(logo)
                .setBorder(Border.NO_BORDER)
                .setPadding(0)
                .setMarginBottom(0);
            
            Cell businessNameCell = new Cell()
                .add(new Paragraph(settings.getBusinessName())
                    .setTextAlignment(TextAlignment.LEFT)
                    .setFontSize(14) // Increased font size
                    .setBold())
                .setBorder(Border.NO_BORDER)
                .setPadding(0)
                .setMarginTop(0);
            
            headerTable.addCell(logoCell);
            headerTable.addCell(businessNameCell);
            document.add(headerTable);

            document.add(new Paragraph("INVOICE #" + invoice.getId())
                .setTextAlignment(TextAlignment.RIGHT)
                .setFontSize(14)
                .setMarginTop(10)
                .setBold());

            Table detailsTable = new Table(2).useAllAvailableWidth();
            detailsTable.setMarginTop(20);
            detailsTable.addCell(createDetailCell("Date:", formatDate(invoice.getTimestamp())));
            detailsTable.addCell(createDetailCell("Client:", invoice.getClientName()));
            detailsTable.addCell(createDetailCell("Cashier:", invoice.getCashier().getUsername()));
            detailsTable.addCell(createDetailCell("Payment Status:", invoice.getStatus().toString()));
            document.add(detailsTable);

            Table productsTable = new Table(UnitValue.createPercentArray(new float[]{40, 20, 20, 20}))
                .useAllAvailableWidth()
                .setMarginTop(20);
            productsTable.setBorderTop(new SolidBorder(ColorConstants.BLACK, 1));
            productsTable.setBorderBottom(new SolidBorder(ColorConstants.BLACK, 1));

            productsTable.addHeaderCell(createHeaderCell("Product"));
            productsTable.addHeaderCell(createHeaderCell("Unit Price"));
            productsTable.addHeaderCell(createHeaderCell("Quantity"));
            productsTable.addHeaderCell(createHeaderCell("Total Price"));

            for (SaleProduct sp : sale.getProducts()) {
                productsTable.addCell(createDataCell(sp.getProduct().getName()));
                productsTable.addCell(createDataCell(String.format("%.2f DH", sp.getProduct().getPrice())));
                productsTable.addCell(createDataCell(String.valueOf(sp.getQuantity())));
                productsTable.addCell(createDataCell(String.format("%.2f DH", sp.getProduct().getPrice() * sp.getQuantity())));
            }

            productsTable.addFooterCell(new Cell(1, 3)
                .add(new Paragraph("Total Amount"))
                .setTextAlignment(TextAlignment.RIGHT)
                .setFontSize(12)
                .setBold());
            productsTable.addFooterCell(new Cell()
                .add(new Paragraph( String.format("%.2f", invoice.getTotalAmount()) + " DH") )
                .setTextAlignment(TextAlignment.RIGHT)
                .setFontSize(12)
                .setBold());
            document.add(productsTable);

            Paragraph footer = new Paragraph()
                .add("Thank you for your business!\n")
                .add("Payment Terms: Due upon receipt | Contact us for any inquiries.")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10)
                .setMarginTop(20)
                .setFixedPosition(36, 20, pdf.getDefaultPageSize().getWidth() - 72);
            document.add(footer);

            document.close();
            System.out.println("Invoice PDF generated: " + new File(fileName).getAbsolutePath());
        } catch (Exception e) {
            System.out.println("Failed to generate invoice PDF: " + e.getMessage());
            throw new RuntimeException("Failed to generate invoice PDF: " + e.getMessage());
        }
    }
   
    private Cell createHeaderCell(String text) {
        return new Cell()
                .add(new Paragraph(text))
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(11)
                .setBold()
                .setPadding(5);
    }

    private Cell createDataCell(String text) {
        return new Cell()
                .add(new Paragraph(text))
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10)
                .setPadding(5);
    }

    private Cell createDetailCell(String label, String value) {
        return new Cell()
                .add(new Paragraph(label + " " + value))
                .setBorder(Border.NO_BORDER)
                .setFontSize(10)
                .setPadding(2);
    }

    private String formatDate(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }
}