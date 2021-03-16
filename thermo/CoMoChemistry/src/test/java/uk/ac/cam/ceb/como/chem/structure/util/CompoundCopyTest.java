/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.cam.ceb.como.chem.structure.util;

import org.junit.Test;
import uk.ac.cam.ceb.como.chem.structure.Atom;
import uk.ac.cam.ceb.como.chem.structure.Bond;
import uk.ac.cam.ceb.como.chem.structure.BondType;
import uk.ac.cam.ceb.como.chem.structure.Compound;

/**
 *
 * @author pb556
 */
public class CompoundCopyTest {

    @Test
    public void copyCompoundTest() throws Exception {
        assert (CompoundComparison.isCH4(CompoundCopy.copy(MockCompounds.getCH4())));
        assert (CompoundComparison.isC2H4(CompoundCopy.copy(MockCompounds.getC2H4())));
        assert (CompoundComparison.isC3H8(CompoundCopy.copy(MockCompounds.getC3H8())));
    }

    @Test
    public void copyAtomTest() {
        Atom aH = new Atom("H");
        Atom aC = new Atom("C");
        Atom aO = new Atom("O");

        assert (CompoundCopy.copy(aH).getElement().getSymbol().equals("H"));
        assert (CompoundCopy.copy(aC).getElement().getSymbol().equals("C"));
        assert (CompoundCopy.copy(aO).getElement().getSymbol().equals("O"));
    }

    @Test
    public void copyBondTest() throws Exception {
        Atom aH = new Atom("H");
        Atom aC = new Atom("C");
        Atom aO = new Atom("O");

        Compound clone = new Compound();
        clone.addAtom(aO);
        clone.addAtom(aH);
        clone.addAtom(aC);

        Bond b1 = Bond.createBond(aH, aO, BondType.SINGLE);
        Bond b2 = Bond.createBond(aH, aC, BondType.SINGLE);
        Bond b3 = Bond.createBond(aO, aC, BondType.DOUBLE);

        assert (CompoundCopy.copy(b1, clone, true).isSimilar(b1));
        assert (CompoundCopy.copy(b2, clone, true).isSimilar(b2));
        assert (CompoundCopy.copy(b3, clone, true).isSimilar(b3));
    }
}
