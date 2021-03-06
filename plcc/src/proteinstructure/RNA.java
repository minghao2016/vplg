/*
 * This file is part of the Visualization of Protein Ligand Graphs (VPLG) software package.
 *
 * Copyright Tim Schäfer 2012. VPLG is free software, see the LICENSE and README files for details.
 *
 * @author ts
 */

package proteinstructure;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import plcc.Main;
import plcc.Settings;
import tools.DP;

/**
 *
 * @author katja
 */
public class RNA extends Molecule implements java.io.Serializable {
    public RNA() {} //Standardkonstruktor
    public RNA (Molecule Mol) {
        this.atoms = Mol.getAtoms();
        this.chain = Mol.getChain();
        this.chainID = Mol.getChainID();
        this.modelID = Mol.getModelID();
        this.iCode = Mol.getiCode();
        this.centerSphereRadius = Mol.getCenterSphereRadius();
        this.pdbNum = Mol.getPdbNum();
        this.dsspNum = Mol.getDsspNum();
        this.Name3 = Mol.getName3();
        this.AAName1 = Mol.getAAName1();
        this.sseString = Mol.getSSEString();
        this.sse = Mol.getSSE();
        this.sseStringDssp = Mol.getSSEStringDssp();
        this.isPartOfDsspSse = Mol.getDsspSseState();
    }  
    
    // ugly implementation b/c of abstract Molecule
    public Boolean isLigand() { return false; }
    public Boolean isAA() { return false; }
    public Integer getType() { return 3; }
    public Boolean contactPossibleWithResidue(Molecule m) { 
        DP.getInstance().w("Function contactPossibleWithResidue not implemented for RNA yet. Returning false");
        return false;
    }
    public Atom getCenterAtom() {
        DP.getInstance().w("Function getCenterAtom not implemented for RNA yet. Returning null");
        return null;
    }
}
