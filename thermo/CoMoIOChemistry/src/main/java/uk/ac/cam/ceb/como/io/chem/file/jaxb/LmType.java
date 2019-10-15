//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.05.10 at 04:17:37 PM BST 
//


package uk.ac.cam.ceb.como.io.chem.file.jaxb;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for lmType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="lmType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="s"/>
 *     &lt;enumeration value="p"/>
 *     &lt;enumeration value="px"/>
 *     &lt;enumeration value="py"/>
 *     &lt;enumeration value="pz"/>
 *     &lt;enumeration value="d"/>
 *     &lt;enumeration value="dxy"/>
 *     &lt;enumeration value="dyz"/>
 *     &lt;enumeration value="dxz"/>
 *     &lt;enumeration value="dx2y2"/>
 *     &lt;enumeration value="dz2"/>
 *     &lt;enumeration value="f"/>
 *     &lt;enumeration value="g"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "lmType")
@XmlEnum
public enum LmType {

    @XmlEnumValue("s")
    S("s"),
    @XmlEnumValue("p")
    P("p"),
    @XmlEnumValue("px")
    PX("px"),
    @XmlEnumValue("py")
    PY("py"),
    @XmlEnumValue("pz")
    PZ("pz"),
    @XmlEnumValue("d")
    D("d"),
    @XmlEnumValue("dxy")
    DXY("dxy"),
    @XmlEnumValue("dyz")
    DYZ("dyz"),
    @XmlEnumValue("dxz")
    DXZ("dxz"),
    @XmlEnumValue("dx2y2")
    DX_2_Y_2("dx2y2"),
    @XmlEnumValue("dz2")
    DZ_2("dz2"),
    @XmlEnumValue("f")
    F("f"),
    @XmlEnumValue("g")
    G("g");
    private final java.lang.String value;

    LmType(java.lang.String v) {
        value = v;
    }

    public java.lang.String value() {
        return value;
    }

    public static LmType fromValue(java.lang.String v) {
        for (LmType c: LmType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
