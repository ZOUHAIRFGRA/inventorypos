package com.fouiguira.pos.inventorypos.services.impl;

import com.fouiguira.pos.inventorypos.entities.Sale;
import com.fouiguira.pos.inventorypos.repositories.SaleRepository;
import com.fouiguira.pos.inventorypos.services.interfaces.SalesService;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
public class SalesServiceImpl implements SalesService {

    private final SaleRepository saleRepository;

    public SalesServiceImpl(SaleRepository saleRepository) {
        this.saleRepository = saleRepository;
    }

    @Override
    public Sale createSale(Sale sale) {
        sale.getProducts().forEach(sp -> sp.setSale(sale)); // Set bidirectional relationship
        return saleRepository.save(sale);
    }

    @Override
    public List<Sale> getAllSales() {
        return saleRepository.findAll();
    }

    @Override
    public List<Sale> getRecentSales(int limit) {
        PageRequest pageable = PageRequest.of(0, limit, Sort.by("timestamp").descending());
        return saleRepository.findByOrderByTimestampDesc(pageable);
    }

    @Override
    public List<Sale> getSalesByDate(LocalDate date) {
        Date start = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        return saleRepository.findByTimestampBetween(start, end);
    }

    @Override
    public double getSalesTotalByDate(LocalDate date) {
        return getSalesByDate(date).stream()
                .mapToDouble(Sale::getTotalPrice)
                .sum();
    }

    @Override
    public void printReceipt(Sale sale) {
        try {
            String fileName = "receipt_" + sale.getId() + ".pdf";
            PdfWriter writer = new PdfWriter(fileName);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("Receipt #" + sale.getId()));
            document.add(new Paragraph("Date: " + sale.getTimestamp()));
            document.add(new Paragraph("Cashier: " + sale.getCashier().getUsername()));
            document.add(new Paragraph("Payment Method: " + sale.getPaymentMethod()));
            document.add(new Paragraph("Total: $" + String.format("%.2f", sale.getTotalPrice())));

            Table table = new Table(3);
            table.addCell(new Cell().add(new Paragraph("Product")));
            table.addCell(new Cell().add(new Paragraph("Quantity")));
            table.addCell(new Cell().add(new Paragraph("Price")));
            sale.getProducts().forEach(sp -> {
                table.addCell(new Cell().add(new Paragraph(sp.getProduct().getName())));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(sp.getQuantity()))));
                table.addCell(new Cell().add(new Paragraph("$" + String.format("%.2f", sp.getProduct().getPrice() * sp.getQuantity()))));
            });
            document.add(table);

            document.close();
            System.out.println("Receipt PDF generated: " + new File(fileName).getAbsolutePath());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate receipt PDF: " + e.getMessage());
        }
    }

    @Override
    public void deleteSale(Long id) {
        saleRepository.deleteById(id);
    }
}