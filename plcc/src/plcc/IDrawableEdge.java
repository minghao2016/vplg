/*
 * This file is part of the Visualization of Protein Ligand Graphs (VPLG) software package.
 *
 * Copyright Tim Schäfer 2015. VPLG is free software, see the LICENSE and README files for details.
 *
 * @author ts
 */
package plcc;

/**
 * An edge that can be drawn using the protein graph drawing method of the PTGL.
 * @author spirit
 */
public interface IDrawableEdge {
    public String getSpatRel();
}
