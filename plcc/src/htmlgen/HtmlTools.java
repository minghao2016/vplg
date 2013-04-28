/*
 * This file is part of the Visualization of Protein Ligand Graphs (VPLG) software package.
 *
 * Copyright Tim Schäfer 2012. VPLG is free software, see the LICENSE and README files for details.
 *
 * @author ts
 */
package htmlgen;

/**
 * Some utility functions for generating HTML strings.
 * @author ts
 */
public class HtmlTools {
    
    public static String link(String targetUrl, String label) {
        return "<a href=\"" + targetUrl + "\">" + label + "</a>";
    }
    
    public static String aname(String id) {
        return "<a name=\"" + id + "\"></a>\n";
    }
    
    public static String tableStart(String[] headerStrings) {
        StringBuilder sb = new StringBuilder();
        sb.append("<table>\n<tr>\n");
        for(String header : headerStrings) {
            sb.append("<th>").append(header).append("</th>\n");
        }        
        sb.append("</tr>\n");
        return sb.toString();
    }
    
    public static String tableRow(String[] cellStrings) {
        StringBuilder sb = new StringBuilder();
        sb.append("<tr>\n");
        for(String header : cellStrings) {
            sb.append("<td>").append(header).append("</td>\n");
        }        
        sb.append("</tr>\n");
        return sb.toString();
    }
    
    public static String tableEnd() {
        return "</table>\n";
    }
    
    public static String uList(String[] strings) {
        StringBuilder sb = new StringBuilder();
        sb.append("<ul>\n");
        for(String header : strings) {
            sb.append("<li>").append(header).append("</li>\n");
        }        
        sb.append("</ul>\n");
        return sb.toString();
    }
    
    public static String uListStart() {
        return "<ul>\n";
    }
    
    public static String uListEnd() {
        return "</ul>\n";
    }
    
    public static String oListStart() {
        return "<ol>\n";
    }
    
    public static String oListEnd() {
        return "</ol>\n";
    }
    
    public static String listItem(String s) {
        return "<li>" + s + "</li>\n";
    }
    
    public static String oList(String[] strings) {
        StringBuilder sb = new StringBuilder();
        sb.append("<ol>\n");
        for(String header : strings) {
            sb.append("<li>").append(header).append("</li>\n");
        }        
        sb.append("</ol>\n");
        return sb.toString();
    }
    
    public static String img(String relPathToImage, String altText) {
        return "<img src=\"" + relPathToImage + "\" alt=\"" + altText + "\">\n";
    }
    
    public static String img(String relPathToImage, String altText, int width, int height) {
        return "<img src=\"" + relPathToImage + "\" alt=\"" + altText + "\" width=\"" + width + "\" height=\"" + height + "\">\n";
    }
    
    public static String paragraph(String contents) {
        return "<p>\n" + contents + "</p>\n";
    }
    
    public static String heading(String contents, int level) {
        return "<h" + level + ">" + contents + "</h" + level + ">\n";
    }
    
    public static String startDiv(String classString) {
        return "<div class=\"" + classString + "\">\n";
    }
    
    public static String endDiv() {
        return "</div>\n";
    }
    
    public static String startSpan(String classString) {
        return "<span class=\"" + classString + "\">\n";
    }
    
    public static String endSpan() {
        return "</span>\n";
    }
    
    public static String startParagraph() {
        return "<p>\n";
    }
    
    public static String endParagraph() {
        return "</p>\n";
    }
    
    public static String hr() {
        return "<hr/>\n";
    }
    
    public static String startBody() {
        return "<body>\n";
    }
     
    public static String endBody() {
        return "</body>\n";
    }
    
    public static String endHtml() {
        return "</html>\n";
    }
    
    public static String comment(String c) {
        return "<!-- " + c + " -->";
    }
    
    public static String italic(String c) {
        return "<span style=\"font-style:italic\">" + c + "</span>";
    }
    
    public static String br() {
        return "<br/>";
    }
    
    public static String brAndNewline() {
        return "<br/>\n";
    }
    
    
}