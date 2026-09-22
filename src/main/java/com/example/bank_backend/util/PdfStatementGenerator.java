package com.example.bank_backend.util;

import com.example.bank_backend.dto.StatementDtos.StatementSummaryResponse;
import com.example.bank_backend.dto.StatementDtos.StatementTransactionItem;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class PdfStatementGenerator {

    public static ByteArrayInputStream generatePdf(StatementSummaryResponse statement) {
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Colors & Fonts
            Color darkBlue = new Color(8, 32, 70);
            Color vibrantBlue = new Color(26, 86, 219);
            Color lightGray = new Color(248, 250, 252);
            Color textDark = new Color(30, 41, 59);

            Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, darkBlue);
            Font fontSubtitle = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY);
            Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
            Font fontBody = FontFactory.getFont(FontFactory.HELVETICA, 9, textDark);
            Font fontBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, textDark);

            // 1. Header Section
            Paragraph title = new Paragraph("NEXORA FINANCIAL CORPORATION", fontTitle);
            title.setAlignment(Element.ALIGN_LEFT);
            document.add(title);

            Paragraph subtitle = new Paragraph("Official Account Statement | Secure • Simple • Digital Banking\n\n", fontSubtitle);
            subtitle.setAlignment(Element.ALIGN_LEFT);
            document.add(subtitle);

            // 2. Account Details Table
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingAfter(15f);

            addInfoRow(infoTable, "Account Holder:", statement.getCustomerName(), fontBold, fontBody);
            addInfoRow(infoTable, "Account Number:", statement.getMaskedAccountNumber(), fontBold, fontBody);
            addInfoRow(infoTable, "Account Type:", statement.getAccountType(), fontBold, fontBody);
            addInfoRow(infoTable, "Statement Period:", statement.getFromDate() + " to " + statement.getToDate(), fontBold, fontBody);
            addInfoRow(infoTable, "Opening Balance:", "INR " + String.format("%,.2f", statement.getOpeningBalance()), fontBold, fontBody);
            addInfoRow(infoTable, "Closing Balance:", "INR " + String.format("%,.2f", statement.getClosingBalance()), fontBold, fontBody);

            document.add(infoTable);

            // 3. Transactions Table
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2.5f, 4.5f, 2.0f, 2.0f, 2.5f});
            table.setSpacingBefore(10f);

            String[] headers = {"Date", "Description", "Debit (INR)", "Credit (INR)", "Balance (INR)"};
            for (String col : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(col, fontHeader));
                cell.setBackgroundColor(vibrantBlue);
                cell.setPadding(6);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            if (statement.getTransactions() != null && !statement.getTransactions().isEmpty()) {
                boolean alternate = false;
                for (StatementTransactionItem item : statement.getTransactions()) {
                    Color bg = alternate ? lightGray : Color.WHITE;

                    PdfPCell dateCell = new PdfPCell(new Phrase(item.getDate() != null ? item.getDate() : "-", fontBody));
                    dateCell.setBackgroundColor(bg);
                    dateCell.setPadding(5);
                    table.addCell(dateCell);

                    PdfPCell descCell = new PdfPCell(new Phrase(item.getDescription() != null ? item.getDescription() : "-", fontBody));
                    descCell.setBackgroundColor(bg);
                    descCell.setPadding(5);
                    table.addCell(descCell);

                    PdfPCell debitCell = new PdfPCell(new Phrase(item.getDebit() != null ? String.format("%,.2f", item.getDebit()) : "-", fontBody));
                    debitCell.setBackgroundColor(bg);
                    debitCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    debitCell.setPadding(5);
                    table.addCell(debitCell);

                    PdfPCell creditCell = new PdfPCell(new Phrase(item.getCredit() != null ? String.format("%,.2f", item.getCredit()) : "-", fontBody));
                    creditCell.setBackgroundColor(bg);
                    creditCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    creditCell.setPadding(5);
                    table.addCell(creditCell);

                    PdfPCell balCell = new PdfPCell(new Phrase(item.getBalance() != null ? String.format("%,.2f", item.getBalance()) : "-", fontBold));
                    balCell.setBackgroundColor(bg);
                    balCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    balCell.setPadding(5);
                    table.addCell(balCell);

                    alternate = !alternate;
                }
            } else {
                PdfPCell emptyCell = new PdfPCell(new Phrase("No transactions recorded for this period.", fontBody));
                emptyCell.setColspan(5);
                emptyCell.setPadding(10);
                emptyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(emptyCell);
            }

            document.add(table);

            // 4. Footer Note
            Paragraph footer = new Paragraph("\n\nThis is a system-generated document and does not require a signature. For support, contact 6382694313.", fontSubtitle);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Error generating statement PDF", e);
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    private static void addInfoRow(PdfPTable table, String label, String value, Font boldFont, Font normalFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, boldFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(3);
        table.addCell(labelCell);

        PdfPCell valCell = new PdfPCell(new Phrase(value != null ? value : "-", normalFont));
        valCell.setBorder(Rectangle.NO_BORDER);
        valCell.setPadding(3);
        table.addCell(valCell);
    }
}