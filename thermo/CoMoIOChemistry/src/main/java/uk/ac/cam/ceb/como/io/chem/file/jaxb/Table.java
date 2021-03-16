//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.05.10 at 04:17:37 PM BST 
//


package uk.ac.cam.ceb.como.io.chem.file.jaxb;

import java.math.BigInteger;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;choice>
 *           &lt;element ref="{http://www.xml-cml.org/schema}arrayList" minOccurs="0"/>
 *           &lt;sequence>
 *             &lt;element ref="{http://www.xml-cml.org/schema}tableHeader"/>
 *             &lt;choice>
 *               &lt;element ref="{http://www.xml-cml.org/schema}tableRowList" maxOccurs="unbounded" minOccurs="0"/>
 *               &lt;element ref="{http://www.xml-cml.org/schema}tableContent" minOccurs="0"/>
 *             &lt;/choice>
 *           &lt;/sequence>
 *         &lt;/choice>
 *       &lt;/sequence>
 *       &lt;attGroup ref="{http://www.xml-cml.org/schema}rows"/>
 *       &lt;attGroup ref="{http://www.xml-cml.org/schema}units"/>
 *       &lt;attGroup ref="{http://www.xml-cml.org/schema}columns"/>
 *       &lt;attGroup ref="{http://www.xml-cml.org/schema}tableType"/>
 *       &lt;attGroup ref="{http://www.xml-cml.org/schema}id"/>
 *       &lt;attGroup ref="{http://www.xml-cml.org/schema}dictRef"/>
 *       &lt;attGroup ref="{http://www.xml-cml.org/schema}convention"/>
 *       &lt;attGroup ref="{http://www.xml-cml.org/schema}title"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "arrayList",
    "tableHeader",
    "tableRowList",
    "tableContent"
})
@XmlRootElement(name = "table")
public class Table {

    protected uk.ac.cam.ceb.como.io.chem.file.jaxb.ArrayList arrayList;
    protected TableHeader tableHeader;
    protected List<TableRowList> tableRowList;
    protected TableContent tableContent;
    @XmlAttribute(name = "rows")
    protected BigInteger rows;
    @XmlAttribute(name = "units")
    protected java.lang.String units;
    @XmlAttribute(name = "columns")
    protected BigInteger columns;
    @XmlAttribute(name = "tableType")
    protected java.lang.String tableType;
    @XmlAttribute(name = "id")
    protected java.lang.String id;
    @XmlAttribute(name = "dictRef")
    protected java.lang.String dictRef;
    @XmlAttribute(name = "convention")
    protected java.lang.String convention;
    @XmlAttribute(name = "title")
    protected java.lang.String title;

    /**
     * Gets the value of the arrayList property.
     * 
     * @return
     *     possible object is
     *     {@link uk.ac.cam.ceb.como.io.chem.file.jaxb.ArrayList }
     *     
     */
    public uk.ac.cam.ceb.como.io.chem.file.jaxb.ArrayList getArrayList() {
        return arrayList;
    }

    /**
     * Sets the value of the arrayList property.
     * 
     * @param value
     *     allowed object is
     *     {@link uk.ac.cam.ceb.como.io.chem.file.jaxb.ArrayList }
     *     
     */
    public void setArrayList(uk.ac.cam.ceb.como.io.chem.file.jaxb.ArrayList value) {
        this.arrayList = value;
    }

    /**
     * Gets the value of the tableHeader property.
     * 
     * @return
     *     possible object is
     *     {@link TableHeader }
     *     
     */
    public TableHeader getTableHeader() {
        return tableHeader;
    }

    /**
     * Sets the value of the tableHeader property.
     * 
     * @param value
     *     allowed object is
     *     {@link TableHeader }
     *     
     */
    public void setTableHeader(TableHeader value) {
        this.tableHeader = value;
    }

    /**
     * Gets the value of the tableRowList property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the tableRowList property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTableRowList().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TableRowList }
     * 
     * 
     */
    public List<TableRowList> getTableRowList() {
        if (tableRowList == null) {
            tableRowList = new java.util.ArrayList<TableRowList>();
        }
        return this.tableRowList;
    }

    /**
     * Gets the value of the tableContent property.
     * 
     * @return
     *     possible object is
     *     {@link TableContent }
     *     
     */
    public TableContent getTableContent() {
        return tableContent;
    }

    /**
     * Sets the value of the tableContent property.
     * 
     * @param value
     *     allowed object is
     *     {@link TableContent }
     *     
     */
    public void setTableContent(TableContent value) {
        this.tableContent = value;
    }

    /**
     * Gets the value of the rows property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getRows() {
        return rows;
    }

    /**
     * Sets the value of the rows property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setRows(BigInteger value) {
        this.rows = value;
    }

    /**
     * Gets the value of the units property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *     
     */
    public java.lang.String getUnits() {
        return units;
    }

    /**
     * Sets the value of the units property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *     
     */
    public void setUnits(java.lang.String value) {
        this.units = value;
    }

    /**
     * Gets the value of the columns property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getColumns() {
        return columns;
    }

    /**
     * Sets the value of the columns property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setColumns(BigInteger value) {
        this.columns = value;
    }

    /**
     * Gets the value of the tableType property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *     
     */
    public java.lang.String getTableType() {
        return tableType;
    }

    /**
     * Sets the value of the tableType property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *     
     */
    public void setTableType(java.lang.String value) {
        this.tableType = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *     
     */
    public java.lang.String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *     
     */
    public void setId(java.lang.String value) {
        this.id = value;
    }

    /**
     * Gets the value of the dictRef property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *     
     */
    public java.lang.String getDictRef() {
        return dictRef;
    }

    /**
     * Sets the value of the dictRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *     
     */
    public void setDictRef(java.lang.String value) {
        this.dictRef = value;
    }

    /**
     * Gets the value of the convention property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *     
     */
    public java.lang.String getConvention() {
        return convention;
    }

    /**
     * Sets the value of the convention property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *     
     */
    public void setConvention(java.lang.String value) {
        this.convention = value;
    }

    /**
     * Gets the value of the title property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *     
     */
    public java.lang.String getTitle() {
        return title;
    }

    /**
     * Sets the value of the title property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *     
     */
    public void setTitle(java.lang.String value) {
        this.title = value;
    }

}
