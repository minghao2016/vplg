/*
 * This file is part of the Visualization of Protein Ligand Graphs (VPLG) software package.
 *
 * Copyright Marcus Kessler 2013. VPLG is free software, see the LICENSE and README files for details.
 *
 * @author Marcus Kessler
 * modified by TS
 */
package proteingraphs;

import graphdrawing.PageLayout;
import graphdrawing.DrawTools;
import graphdrawing.DrawResult;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sourceforge.spargel.datastructures.UAdjListGraph;
import net.sourceforge.spargel.writers.GMLWriter;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import io.DBManager;
import io.FileParser;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import plcc.Settings;
import proteinstructure.Chain;
import proteinstructure.Residue;
import proteinstructure.SSE;
import tools.DP;

/**
 *
 * @author marcus
 */
public class ComplexGraph extends UAdjListGraph {

    private final int numberChains;  // Number of chains
    public Map<Edge, String[]> chainNamesInEdge;
    public Map<Edge, Integer> numHelixHelixInteractionsMap;
    public Map<Edge, Integer> numHelixStrandInteractionsMap;
    public Map<Edge, Integer> numHelixCoilInteractionsMap;
    public Map<Edge, Integer> numHelixLigandInteractionsMap;
    public Map<Edge, Integer> numStrandStrandInteractionsMap;
    public Map<Edge, Integer> numStrandCoilInteractionsMap;
    public Map<Edge, Integer> numStrandLigandInteractionsMap;
    public Map<Edge, Integer> numCoilCoilInteractionsMap;
    public Map<Edge, Integer> numCoilLigandInteractionsMap;
    public Map<Edge, Integer> numLigandLigandInteractionsMap;
    public Map<Edge, Integer> numAllInteractionsMap;
    public Map<Edge, Integer> numDisulfidesMap;
    public Map<Vertex, String> proteinNodeMap;
    public Map<Vertex, String> molMap;  // contains for each vertex (= protein chain) the corresponding molecule name
    public Map<List<Integer>, Integer> numSSEContacts;
    public Map<List<Integer>, List<String>> numSSEContactChainNames;
    
    private final Boolean createContactInfo;
    private ArrayList<String> contactInfo;

    private Integer[][] numChainInteractions;
    private Integer[][] homologueChains;
    private final Set<String> molIDs;  // Contains the mol IDs for all chains. Used to get number of mol IDs (= size)
    private final String[] chainResAASeq;
    public Integer neglectedEdges;

    /**
     * The RCSB PDB id this graph is based on.
     */
    private final String pdbid;

    /**
     * Constructor.
     * @param pdbid RSCB PDB ID
     * @param chains
     * @param resContacts
     * @param createConInfo Whether contact info for writing of GML should be created. Usually pass plcc_B_writeComplexContactCSV setting.
     */
    public ComplexGraph(String pdbid, List<Chain> chains, List<MolContactInfo> resContacts, Boolean createConInfo) {
        this.pdbid = pdbid;
        numberChains = chains.size();
        createContactInfo = createConInfo;
        
        numHelixHelixInteractionsMap = createEdgeMap();
        numHelixStrandInteractionsMap = createEdgeMap();
        numHelixCoilInteractionsMap = createEdgeMap();
        numHelixLigandInteractionsMap = createEdgeMap();
        numStrandStrandInteractionsMap = createEdgeMap();
        numStrandCoilInteractionsMap = createEdgeMap();
        numStrandLigandInteractionsMap = createEdgeMap();
        numCoilCoilInteractionsMap = createEdgeMap();
        numCoilLigandInteractionsMap = createEdgeMap();
        numLigandLigandInteractionsMap = createEdgeMap();
        numAllInteractionsMap = createEdgeMap();
        numDisulfidesMap = createEdgeMap();
        proteinNodeMap = createVertexMap();
        molMap = createVertexMap();
        molIDs = new HashSet<>();
        chainNamesInEdge = createEdgeMap();
        numSSEContacts = new HashMap<>();
        numSSEContactChainNames = new HashMap<>();

        neglectedEdges = 0;
        
        chainResAASeq = new String[numberChains];
        
        createVertices(chains);
        createHomologueChainsMatrix(chains);
        calculateNumChainInteractions(resContacts);
        createEdges(resContacts);
    }
    
    
    /**
     * Creates the vertices, fills corresponding maps and fills amino acid sequence.
     * @param chains All chains of this complex graph
     */
    private void createVertices(List<Chain> chains) {
        for(Integer i = 0; i < chains.size(); i++) {
            Vertex v = createVertex();
            proteinNodeMap.put(v, chains.get(i).getPdbChainID());
            molMap.put(v, FileParser.getMetaInfo(pdbid, chains.get(i).getPdbChainID()).getMolName());  // get the mol name from the ProtMetaInfo
            molIDs.add(FileParser.getMetaInfo(pdbid, chains.get(i).getPdbChainID()).getMolName());

            // get AA sequence string for each chainName
            for(Residue resi : chains.get(i).getResidues()) {
                
                if ( ! Settings.get("plcc_S_ligAACode").equals(resi.getAAName1())) {  // Skip ligands to preserve sequence identity. What to do with "_B_", "_Z_", "_X_" (B,Z,X)?
                    if (chainResAASeq[i] != null) {
                        chainResAASeq[i] = chainResAASeq[i] + resi.getAAName1();
                    } else {
                        chainResAASeq[i] = resi.getAAName1();
                    }
                }
            }
        }
    }
    
    
    /**
     * Creates and fills homologueChains matrix.
     * @param chains All chains of this complex graph
     */
    private void createHomologueChainsMatrix(List<Chain> chains) {
    if(! Settings.getBoolean("plcc_B_silent")) {
            System.out.println("  Computing CG contacts.");
        }
    
        homologueChains = new Integer[chains.size()][chains.size()];
        if (chains.size() > 1) {
            for (Integer i = 0; i < chains.size(); i++) {
                
                for (Integer j = 0; j < chains.size(); j++) {

                    String compareChainID = chains.get(j).getPdbChainID();
                    // make sure no chainName is matched with itself 
                    if (chains.get(i).getHomologues() != null) {
                        if ((chains.get(i).getHomologues().contains(compareChainID)) && (!Objects.equals(i, j))) {
                            homologueChains[i][j] = 1;
                            // fill bottom-left and top-right triangle of matrix (required since chains are unordered b/c of speedup)
                            homologueChains[j][i] = 1;
                        } else {
                            homologueChains[i][j] = 0;
                        }
                    } else {
                        homologueChains[i][j] = 0;
                    }
                }
            }
        }
    }
    
    
    /**
     * Creates and fills number of chain interactions matrix.
     * @param resContacts Residue contacts
     */
    private void calculateNumChainInteractions(List<MolContactInfo> resContacts) {
        numChainInteractions = new Integer[numberChains][numberChains];
        for(Integer i = 0; i < resContacts.size(); i++){
            ComplexGraph.Vertex chainA = getVertexFromChain(resContacts.get(i).getMolA().getChainID());
            ComplexGraph.Vertex chainB = getVertexFromChain(resContacts.get(i).getMolB().getChainID());
                      
            Integer chainAint = Integer.parseInt(chainA.toString());
            Integer chainBint = Integer.parseInt(chainB.toString());
            
            // We only want interchain contacts
            if (!chainA.equals(chainB)){
                if(numChainInteractions[chainAint][chainBint] == null){
                    numChainInteractions[chainAint][chainBint] = 1;
                    numChainInteractions[chainBint][chainAint] = 1;
                } else {
                    numChainInteractions[chainAint][chainBint]++;
                    numChainInteractions[chainBint][chainAint]++;
                }
            }
        }
    }
    
    
    /**
     * Creates edges and if required contact info for csv.
     * @param resContacts Residue contacts
     */
    private void createEdges(List<MolContactInfo> resContacts) {
        if (createContactInfo) {
            contactInfo = new ArrayList<>();
            contactInfo.add("ChainA;ChainB;ResNameA;ResNameB;resTypeA;resTypeB;BB;BC;BL;CB;CL;CC;HB1;HB2;LB;LC;LL;"
                  + "BBDist;BCDist;BLDist;CBDist;CLDist;CCDist;HB1Dist;HB2Dist;LBDist;LCDist;LLDist");
        }
        
        // inform here if edge threshold is >1 (below it would result in multiple prints)
        if (Settings.getInteger("plcc_I_cg_contact_threshold") > 1) {    
            if (! Settings.getBoolean("plcc_B_silent")) {
                System.out.println("  Complex graph contact threshold for edges is set to "
                        + Settings.getInteger("plcc_I_cg_contact_threshold").toString()
                        + ". Resulting graphs may differ from default setting '1' where all "
                        + "edges are drawn.");
            }
        }
        
        // create edges for all contacts
        for(Integer i = 0; i < resContacts.size(); i++) {
            ComplexGraph.Vertex chainA = getVertexFromChain(resContacts.get(i).getMolA().getChainID());
            ComplexGraph.Vertex chainB = getVertexFromChain(resContacts.get(i).getMolB().getChainID());
            
            Integer chainAint = Integer.parseInt(chainA.toString());
            Integer chainBint = Integer.parseInt(chainB.toString());
            
            // We only want interchain contacts with a certain threshold of contacts
            if (chainsHaveEnoughContacts(chainAint, chainBint)){
                
                MolContactInfo curResCon = resContacts.get(i);
                
                // Die Datenkrake
                String chainAString = curResCon.getMolA().getChainID().toString();
                String chainBString = curResCon.getMolB().getChainID().toString();
                String resNameA = curResCon.getName3A();
                String resNameB = curResCon.getName3B();
                String resTypeA = curResCon.getMolA().getSSETypePlcc();
                String resTypeB = curResCon.getMolB().getSSETypePlcc();
                
                String BBDist = curResCon.getBBContactDist().toString();
                String BCDist = curResCon.getBCContactDist().toString();
                String BLDist = curResCon.getBLContactDist().toString();
                String CBDist = curResCon.getCBContactDist().toString();
                String CLDist = curResCon.getCLContactDist().toString();
                String CCDist = curResCon.getCCContactDist().toString();
                String HB1Dist = curResCon.getHB1Dist().toString();
                String HB2Dist = curResCon.getHB2Dist().toString();
                String LBDist = curResCon.getLBContactDist().toString();
                String LCDist = curResCon.getLCContactDist().toString();
                String LLDist = curResCon.getLLContactDist().toString();
                                
                String numBB = curResCon.getNumContactsBB().toString();
                String numBC = curResCon.getNumContactsBC().toString();
                String numBL = curResCon.getNumContactsBL().toString();
                String numCB = curResCon.getNumContactsCB().toString();
                String numCL = curResCon.getNumContactsCL().toString();
                String numCC = curResCon.getNumContactsCC().toString();
                String numHB1 = curResCon.getNumContactsHB1().toString();
                String numHB2 = curResCon.getNumContactsHB2().toString();
                String numLB = curResCon.getNumContactsLB().toString();
                String numLC = curResCon.getNumContactsLC().toString();
                String numLL = curResCon.getNumContactsLL().toString();
                
                
                // Only if both residues belong to a SSE..
                if (curResCon.getMolA().getSSE() != null && curResCon.getMolB().getSSE() != null) {
                    //.. and are of type helix or strand
                    if ((!Objects.equals(curResCon.getMolA().getSSE().getSSETypeInt(), SSE.SSECLASS_NONE)
                            && !Objects.equals(curResCon.getMolA().getSSE().getSSETypeInt(), SSE.SSECLASS_OTHER))
                            && (!Objects.equals(curResCon.getMolB().getSSE().getSSETypeInt(), SSE.SSECLASS_NONE)
                            && !Objects.equals(curResCon.getMolB().getSSE().getSSETypeInt(), SSE.SSECLASS_OTHER))) {

                        Integer ResASseDsspNum = curResCon.getMolA().getSSE().getStartDsspNum();
                        Integer ResBSseDsspNum = curResCon.getMolB().getSSE().getStartDsspNum();

                        
                        Integer tmp;
                        String tmpName;
                        if (ResASseDsspNum > ResBSseDsspNum) {
                            tmp = ResBSseDsspNum;
                            tmpName = chainBString;

                            ResBSseDsspNum = ResASseDsspNum;
                            chainBString = chainAString;

                            ResASseDsspNum = tmp;
                            chainAString = tmpName;
                        }

                        List<Integer> SSEPair = Arrays.asList(ResASseDsspNum, ResBSseDsspNum);
                        List<String> NamePair = Arrays.asList(chainAString, chainBString);

                        numSSEContactChainNames.put(SSEPair, NamePair);
                        if (numSSEContacts.get(SSEPair) == null) {
                            numSSEContacts.put(SSEPair, 1);
                        } else {
                            numSSEContacts.put(SSEPair, numSSEContacts.get(SSEPair) + 1);
                        }
                    }

                }
                      
                                
                //This is for CSV output only. Like the most of the code above.
                if (createContactInfo) {
                contactInfo.add(chainAString + ";" + chainBString + ";" + resNameA + ";" + resNameB + ";" + 
                            resTypeA + ";" + resTypeB + ";" + numBB + ";" + numBC + ";" + numBL + ";" + 
                            numCB + ";" + numCL + ";" + numCC + ";" + numHB1 + ";" + numHB2 + ";" + 
                            numLB + ";" + numLC + ";" + numLL + ";" + BBDist + ";" + BCDist + ";" + 
                            BLDist + ";" + CBDist + ";" + CLDist + ";" + CCDist + ";" + HB1Dist + ";" + 
                            HB2Dist + ";" + LBDist + ";" + LCDist + ";" + LLDist);
                }

                String[] chainPair = {chainAString, chainBString};
                
                if (getEdge(chainA, chainB) == null){
                    // We don't have an edge yet, but need one, so create an edge
                    ComplexGraph.Edge e1 = createEdge(chainA, chainB);
                    chainNamesInEdge.put(e1, chainPair);
                    numAllInteractionsMap.put(e1, 1); // rather weird: 1 is added here, therefore the first contact is skipped later. We will have to change this and sum up the others in the end, (which of them depending on the graph type)
                    numHelixHelixInteractionsMap.put(e1, 0);
                    numHelixStrandInteractionsMap.put(e1, 0);
                    numHelixCoilInteractionsMap.put(e1, 0);
                    numHelixLigandInteractionsMap.put(e1, 0);
                    numStrandStrandInteractionsMap.put(e1, 0);
                    numStrandCoilInteractionsMap.put(e1, 0);
                    numStrandLigandInteractionsMap.put(e1, 0);
                    numCoilCoilInteractionsMap.put(e1, 0);
                    numCoilLigandInteractionsMap.put(e1, 0);
                    numLigandLigandInteractionsMap.put(e1, 0);
                    
                    
                    if (resContacts.get(i).getMolA().getSSE()!=null){
                        // the 1st residue of this contact belongs to a valid PTGL SSE
                        int firstSSEClass = resContacts.get(i).getMolA().getSSE().getSSETypeInt();
                        switch (firstSSEClass){                            
                            case 1: // SSECLASS_HELIX
                                if (resContacts.get(i).getMolB().getSSE()!=null){
                                    int secondSSE = resContacts.get(i).getMolB().getSSE().getSSETypeInt();
                                    switch (secondSSE){
                                        case 1: // SSECLASS_HELIX
                                            numHelixHelixInteractionsMap.put(e1, 1);  // we are creating a new edge, so this is the first contact
                                            break;
                                        case 2: // SSECLASS_BETASTRAND
                                            numHelixStrandInteractionsMap.put(e1, 1);
                                            break;
                                        case 3: // SSECLASS_LIGAND
                                            //System.out.println("Ligand Contact");
                                            numHelixLigandInteractionsMap.put(e1, 1);
                                            break;
                                        case 4:
                                            numHelixCoilInteractionsMap.put(e1, 1);
                                            break;
                                    }
                                }
                                else{
                                    numHelixCoilInteractionsMap.put(e1, 1);
                                    //System.out.println("Loop Contact");
                                }
                                break;
                            case 2: // SSECLASS_BETASTRAND
                                if (resContacts.get(i).getMolB().getSSE()!=null){
                                    int secondSSE = resContacts.get(i).getMolB().getSSE().getSSETypeInt();
                                    switch (secondSSE){
                                        case 1: // SSECLASS_HELIX
                                            numHelixStrandInteractionsMap.put(e1, 1);
                                            break;
                                        case 2: // SSECLASS_BETASTRAND
                                            numStrandStrandInteractionsMap.put(e1, 1);
                                            break;
                                        case 3: // SSECLASS_LIGAND
                                            numStrandLigandInteractionsMap.put(e1, 1);
                                            //System.out.println("Ligand Contact");
                                            break;
                                        case 4:
                                            //System.out.println("Other Contact");
                                            numStrandCoilInteractionsMap.put(e1, 1);
                                            break;
                                    }
                                }
                                else{
                                    numStrandCoilInteractionsMap.put(e1, 1);
                                    //System.out.println("Loop Contact");
                                }
                                break;
                            case 3: // SSECLASS_LIGAND
                                //System.out.println("Ligand Contact");
                                if (resContacts.get(i).getMolB().getSSE()!=null){
                                    int secondSSE = resContacts.get(i).getMolB().getSSE().getSSETypeInt();
                                    switch (secondSSE){
                                        case 1: // SSECLASS_HELIX
                                            numHelixLigandInteractionsMap.put(e1, 1);
                                            break;
                                        case 2: // SSECLASS_BETASTRAND
                                            numStrandLigandInteractionsMap.put(e1, 1);
                                            break;
                                        case 3: // SSECLASS_LIGAND
                                            numLigandLigandInteractionsMap.put(e1, 1);
                                            //System.out.println("Ligand Contact");
                                            break;
                                        case 4:
                                            //System.out.println("Other Contact");
                                            numCoilLigandInteractionsMap.put(e1, 1);
                                            break;
                                    }
                                }
                                else{
                                    numCoilLigandInteractionsMap.put(e1, 1);
                                    //System.out.println("Loop Contact");
                                }
                                break;
                            case 4:
                                //System.out.println("Other Contact");
                                if (resContacts.get(i).getMolB().getSSE()!=null){
                                    int secondSSE = resContacts.get(i).getMolB().getSSE().getSSETypeInt();
                                    switch (secondSSE){
                                        case 1: // SSECLASS_HELIX
                                            numHelixCoilInteractionsMap.put(e1, 1);
                                            break;
                                        case 2: // SSECLASS_BETASTRAND
                                            numStrandCoilInteractionsMap.put(e1, 1);
                                            break;
                                        case 3: // SSECLASS_LIGAND
                                            numCoilLigandInteractionsMap.put(e1, 1);
                                            //System.out.println("Ligand Contact");
                                            break;
                                        case 4:
                                            //System.out.println("Other Contact");
                                            numCoilCoilInteractionsMap.put(e1, 1);
                                            break;
                                    }
                                }
                                else{
                                    numCoilCoilInteractionsMap.put(e1, 1);
                                    //System.out.println("Loop Contact");
                                }
                                break;
                        }
                    }
                    else{
                        // the first residue of this contact does NOT belong to a valid PTGL SSE, i.e., it is a coil
                        if (resContacts.get(i).getMolB().getSSE()!=null){
                            int secondSSE = resContacts.get(i).getMolB().getSSE().getSSETypeInt();
                            switch (secondSSE){
                                case 1: // SSECLASS_HELIX
                                    numHelixCoilInteractionsMap.put(e1, 1);
                                    break;
                                case 2: // SSECLASS_BETASTRAND
                                    numStrandCoilInteractionsMap.put(e1, 1);
                                    break;
                                case 3: // SSECLASS_LIGAND
                                    //System.out.println("Ligand Contact");
                                    numCoilLigandInteractionsMap.put(e1, 1);
                                    break;
                                case 4:
                                    //System.out.println("Other Contact");
                                    numCoilCoilInteractionsMap.put(e1, 1);
                                    break;
                            }

                        }
                        else{
                            numCoilCoilInteractionsMap.put(e1, 1);
                            //System.out.println("Loop-loop Contact");
                        }
                    }
                    //System.out.println("Contact found between chainName " + resContacts.get(i).getMolA().getChainID() + " and chainName " + resContacts.get(i).getMolB().getChainID());
                }
                else{
                    // We already have an edge, just adjust values
                    numAllInteractionsMap.put(getEdge(chainA, chainB), numAllInteractionsMap.get(getEdge(chainA, chainB)) + 1);
                    if (resContacts.get(i).getMolA().getSSE()!=null){
                        // first residue of contact belongs to valid PTGL SSE, i.e., is NOT a coil
                        int firstSSE = resContacts.get(i).getMolA().getSSE().getSSETypeInt();
                        switch (firstSSE){
                            case 1: // SSECLASS_HELIX
                                if (resContacts.get(i).getMolB().getSSE()!=null){
                                    int secondSSE = resContacts.get(i).getMolB().getSSE().getSSETypeInt();
                                    switch (secondSSE){
                                        case 1: // SSECLASS_HELIX
                                            numHelixHelixInteractionsMap.put(getEdge(chainA, chainB), numHelixHelixInteractionsMap.get(getEdge(chainA, chainB)) + 1);
                                            break;
                                        case 2: // SSECLASS_BETASTRAND
                                            numHelixStrandInteractionsMap.put(getEdge(chainA, chainB), numHelixStrandInteractionsMap.get(getEdge(chainA, chainB)) + 1);
                                            break;
                                        case 3: // SSECLASS_LIGAND
                                            //System.out.println("Ligand Contact");
                                            numHelixLigandInteractionsMap.put(getEdge(chainA, chainB), numHelixLigandInteractionsMap.get(getEdge(chainA, chainB)) + 1);
                                            break;
                                        case 4:
                                            //System.out.println("Other Contact");
                                            numHelixCoilInteractionsMap.put(getEdge(chainA, chainB), numHelixCoilInteractionsMap.get(getEdge(chainA, chainB)) + 1);
                                            break;
                                    }
                                }
                                else{
                                    numHelixCoilInteractionsMap.put(getEdge(chainA, chainB), numHelixCoilInteractionsMap.get(getEdge(chainA, chainB)) + 1);
                                    //System.out.println("Loop Contact");
                                }
                                break;
                            case 2: // SSECLASS_BETASTRAND
                                if (resContacts.get(i).getMolB().getSSE()!=null){
                                    int secondSSE = resContacts.get(i).getMolB().getSSE().getSSETypeInt();
                                    switch (secondSSE){
                                        case 1: // SSECLASS_HELIX
                                            numHelixStrandInteractionsMap.put(getEdge(chainA, chainB), numHelixStrandInteractionsMap.get(getEdge(chainA, chainB)) + 1);
                                            break;
                                        case 2: // SSECLASS_BETASTRAND
                                            numStrandStrandInteractionsMap.put(getEdge(chainA, chainB), numStrandStrandInteractionsMap.get(getEdge(chainA, chainB)) + 1);
                                            break;
                                        case 3: // SSECLASS_LIGAND
                                            //System.out.println("Ligand Contact");
                                            numStrandLigandInteractionsMap.put(getEdge(chainA, chainB), numStrandLigandInteractionsMap.get(getEdge(chainA, chainB)) + 1);
                                            break;
                                        case 4:
                                            //System.out.println("Other Contact");
                                            numStrandCoilInteractionsMap.put(getEdge(chainA, chainB), numStrandCoilInteractionsMap.get(getEdge(chainA, chainB)) + 1);
                                            break;
                                    }
                                }
                                else{
                                    numStrandCoilInteractionsMap.put(getEdge(chainA, chainB), numStrandCoilInteractionsMap.get(getEdge(chainA, chainB)) + 1);
                                    //System.out.println("Loop Contact");
                                }
                                break;
                            case 3: // SSECLASS_LIGAND
                                //System.out.println("Ligand Contact");
                                if (resContacts.get(i).getMolB().getSSE()!=null){
                                    int secondSSE = resContacts.get(i).getMolB().getSSE().getSSETypeInt();
                                    switch (secondSSE){
                                        case 1: // SSECLASS_HELIX
                                            numHelixLigandInteractionsMap.put(getEdge(chainA, chainB), numHelixLigandInteractionsMap.get(getEdge(chainA, chainB)) + 1);
                                            break;
                                        case 2: // SSECLASS_BETASTRAND
                                            numStrandLigandInteractionsMap.put(getEdge(chainA, chainB), numStrandLigandInteractionsMap.get(getEdge(chainA, chainB)) + 1);
                                            break;
                                        case 3: // SSECLASS_LIGAND
                                            //System.out.println("Ligand Contact");
                                            numLigandLigandInteractionsMap.put(getEdge(chainA, chainB), numLigandLigandInteractionsMap.get(getEdge(chainA, chainB)) + 1);
                                            break;
                                        case 4:
                                            //System.out.println("Other Contact");
                                            numCoilLigandInteractionsMap.put(getEdge(chainA, chainB), numCoilLigandInteractionsMap.get(getEdge(chainA, chainB)) + 1);
                                            break;
                                    }
                                }
                                else{
                                    numCoilLigandInteractionsMap.put(getEdge(chainA, chainB), numCoilLigandInteractionsMap.get(getEdge(chainA, chainB)) + 1);
                                    //System.out.println("Loop Contact");
                                }
                                break;
                            case 4:
                                //System.out.println("Other Contact");
                                if (resContacts.get(i).getMolB().getSSE()!=null){
                                    int secondSSE = resContacts.get(i).getMolB().getSSE().getSSETypeInt();
                                    switch (secondSSE){
                                        case 1: // SSECLASS_HELIX
                                            numHelixCoilInteractionsMap.put(getEdge(chainA, chainB), numHelixCoilInteractionsMap.get(getEdge(chainA, chainB)) + 1);
                                            break;
                                        case 2: // SSECLASS_BETASTRAND
                                            numStrandCoilInteractionsMap.put(getEdge(chainA, chainB), numStrandCoilInteractionsMap.get(getEdge(chainA, chainB)) + 1);
                                            break;
                                        case 3: // SSECLASS_LIGAND
                                            //System.out.println("Ligand Contact");
                                            numCoilLigandInteractionsMap.put(getEdge(chainA, chainB), numCoilLigandInteractionsMap.get(getEdge(chainA, chainB)) + 1);
                                            break;
                                        case 4:
                                            //System.out.println("Other Contact");
                                            numCoilCoilInteractionsMap.put(getEdge(chainA, chainB), numCoilCoilInteractionsMap.get(getEdge(chainA, chainB)) + 1);
                                            break;
                                    }
                                }
                                else{
                                    numCoilCoilInteractionsMap.put(getEdge(chainA, chainB), numCoilCoilInteractionsMap.get(getEdge(chainA, chainB)) + 1);
                                    //System.out.println("Loop Contact");
                                }
                                break;
                        }
                    }
                    else{   // first residue of contact does NOT belong to valid PTGL SSE, i.e., is a coil
                        if (resContacts.get(i).getMolB().getSSE()!=null){
                            int secondSSE = resContacts.get(i).getMolB().getSSE().getSSETypeInt();
                            switch (secondSSE){
                                case 1: // SSECLASS_HELIX
                                    numHelixCoilInteractionsMap.put(getEdge(chainA, chainB), numHelixCoilInteractionsMap.get(getEdge(chainA, chainB)) + 1);
                                    break;
                                case 2: // SSECLASS_BETASTRAND
                                    numStrandCoilInteractionsMap.put(getEdge(chainA, chainB), numStrandCoilInteractionsMap.get(getEdge(chainA, chainB)) + 1);
                                    break;
                                case 3: // SSECLASS_LIGAND
                                    //System.out.println("Ligand Contact");
                                    numCoilLigandInteractionsMap.put(getEdge(chainA, chainB), numCoilLigandInteractionsMap.get(getEdge(chainA, chainB)) + 1);
                                    break;
                                case 4:
                                    //System.out.println("Other Contact");
                                    numCoilCoilInteractionsMap.put(getEdge(chainA, chainB), numCoilCoilInteractionsMap.get(getEdge(chainA, chainB)) + 1);
                                    break;
                            }
                        }
                        else{
                            numCoilCoilInteractionsMap.put(getEdge(chainA, chainB), numCoilCoilInteractionsMap.get(getEdge(chainA, chainB)) + 1);
                            //System.out.println("Loop Contact");
                        }
                    }
                }
                
                // TODO: Test by Tim: maybe we should delete the edge if it has no contacts:
                
                if(numAllInteractionsMap.get(getEdge(chainA, chainB)) == 0) {
                    removeEdge(getEdge(chainA, chainB));
                }
            } else {
                neglectedEdges++; // TODO: so wrong...
            }
        }
    }
    

    public Vertex getVertexFromChain(String chainID) {
        Iterator<Vertex> vertIter = this.getVertices().iterator();
        Vertex nextVert;
        while (vertIter.hasNext()) {
            nextVert = vertIter.next();
            if (ComplexGraph.this.proteinNodeMap.get(nextVert).equals(chainID)) {
                return nextVert;
            }
        }
        return null;
    }

    public String getPDBID() {
        return this.pdbid;
    }

    public Boolean chainsHaveEnoughContacts(Integer A, Integer B) {
        if (this.numChainInteractions[A][B] != null) {
            if (this.numChainInteractions[A][B] >= Settings.getInteger("plcc_I_cg_contact_threshold")) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }


    public void writeSSEComplexContactInfoToDB(String pdbid) {

    //DBManager.writeSSEComplexContactToDB()
        String chainA;
        String chainB;

        Integer numHelixHelixInteractions;
        Integer numHelixStrandInteractions;
        Integer numHelixLoopInteractions;
        Integer numSSInteractions;
        Integer numStrandLoopInteractions;
        Integer numLoopLoopInteractions;
        Integer numAllInteractions;
        Integer numDisulfides;

        int countInsert = 0;
        int countFail = 0;
        Boolean retVal = false;
        for (Map.Entry<List<Integer>, Integer> pair : this.numSSEContacts.entrySet()) {

            List<Integer> curSSEs = (List<Integer>) pair.getKey();
            chainA = this.numSSEContactChainNames.get(curSSEs).get(0);
            chainB = this.numSSEContactChainNames.get(curSSEs).get(1);
            
            Integer sse1_dssp_start = (Integer)curSSEs.get(0);
            Integer sse2_dssp_start = (Integer)curSSEs.get(1);
            Integer contactCount = (Integer)pair.getValue();
            
          
            Boolean res = false;
            
            
            // this action could result in an error due to the definition of a PTGL SSE
            // e.g. the SSE is too short and is merged to another SSE or not defined in the DB
            try {
                res = DBManager.writeSSEComplexContactToDB(pdbid, chainA, chainB, sse1_dssp_start, sse2_dssp_start, contactCount);
                //it.remove(); // avoids a ConcurrentModificationException
                if(res) {
                    countInsert++;
                    retVal = true;
                }
                else {
                    countFail++;
                    retVal = false;
                }
            } catch (SQLException ex) {
                DP.getInstance().e("ComplexGraph", "writeSSEComplexContactInfoToDB: SQL exception: '" + ex.getMessage() + "'.");
                Logger.getLogger(ComplexGraph.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if(! Settings.getBoolean("plcc_B_silent")) {
            System.out.println("    SSE Contacts written to DB: " + countInsert + " inserted, " + countFail + " skipped (contact involved coils).");
        }
    }

    /**
     * Separates interaction maps and associates them with a chain name then
     * calls DBManager.writeComplexContactToDB()
     *
     * @return true if DBManager is succesfull
     */
    public boolean writeChainComplexContactInfoToDB() {

        String chainA;
        String chainB;

        Integer numHelixHelixInteractions;
        Integer numHelixStrandInteractions;
        Integer numHelixLoopInteractions;
        Integer numStrandStrandInteractions;
        Integer numStrandLoopInteractions;
        Integer numLoopLoopInteractions;
        Integer numAllInteractions;
        Integer numDisulfides;

        for (Map.Entry pair : this.chainNamesInEdge.entrySet()) {

            Edge curEdge = (Edge) pair.getKey();
            String[] chainPair = (String[]) pair.getValue();

            chainA = chainPair[0];
            chainB = chainPair[1];

            // interactions with ligands are NOT yet written to the database!
            numHelixHelixInteractions = this.numHelixHelixInteractionsMap.get(curEdge);
            numHelixStrandInteractions = this.numHelixStrandInteractionsMap.get(curEdge);
            numHelixLoopInteractions = this.numHelixCoilInteractionsMap.get(curEdge);
            numStrandStrandInteractions = this.numStrandStrandInteractionsMap.get(curEdge);
            numStrandLoopInteractions = this.numStrandCoilInteractionsMap.get(curEdge);
            numLoopLoopInteractions = this.numCoilCoilInteractionsMap.get(curEdge);
            numDisulfides = this.numDisulfidesMap.get(curEdge);
            numAllInteractions = this.numAllInteractionsMap.get(curEdge);

            Integer[] interactionNums = {numHelixHelixInteractions, numHelixStrandInteractions, numHelixLoopInteractions,
                numStrandStrandInteractions, numStrandLoopInteractions, numLoopLoopInteractions,
                numDisulfides, numAllInteractions};

            // make sure no entry is null or something shitty
            for (int i = 0; i < interactionNums.length; i++) {
                if (interactionNums[i] == null) {
                    interactionNums[i] = 0;
                }
            }

            try {
                DBManager.writeChainComplexContactToDB(pdbid, chainA, chainB, interactionNums);
                //it.remove(); // avoids a ConcurrentModificationException
            } catch (SQLException ex) {
                Logger.getLogger(ComplexGraph.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        }
        return true;
    }

    // Old color function: was insensitive to the case of many vertices and few MolIDs (Homologues)
    /*
    private float getUniqueColor(Integer numVertices) {
        float step = 360 / (numVertices + 1); // +1 to avoid double red
        float hue = (lastColorStep + step) / 360;
        lastColorStep += step;      
        return hue;
    }
    */
    
    
    /**
     * Returns a hue based on the molID in relation to the total number of molIDs. Requires molIDs to be filled.
     * All homologous chains get the same color and the colors of all Homologues are distinct with equal distance to each other. 
     * @param molID
     * @return 
     */
    public float getUniqueHue(Integer molID) {
        //return (360f / (molIDs.size()) * molID) / 360f;
        return 1f / molIDs.size() * (molID - 1);
    }
    

    /**
     * Draws a complex graph
     * @param nonProteinGraph whether the graph is a non-protein graph and should be drawn black and white
     * @param cg the complex graph
     * @param molInfoForChains info mapping chain IDs (like "A") to their macromolecule (MOL_ID in PDB file, e.g., "1"). Give an empty one if you dont know
     * @return a draw result
     */
    private static DrawResult drawChainLevelComplexGraphG2D(Boolean nonProteinGraph, ComplexGraph cg, Map<String, String> molInfoForChains) {
        
        
        Integer numVerts = cg.getVertices().size();

        Boolean bw = nonProteinGraph;

    // All these values are in pixels
        // page setup
        PageLayout pl = new PageLayout(numVerts);
        Position2D vertStart = pl.getVertStart();

    // ------------------------- Prepare stuff -------------------------
        // TYPE_INT_ARGB specifies the image format: 8-bit RGBA packed into integer pixels
        //BufferedImage bi = new BufferedImage(pl.getPageWidth(), pl.getPageHeight(), BufferedImage.TYPE_INT_ARGB);
        SVGGraphics2D ig2;

        //if(Settings.get("plcc_S_img_output_format").equals("SVG")) {                    
        // Apache Batik SVG library, using W3C DOM tree implementation
        // Get a DOMImplementation.
        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
        // Create an instance of org.w3c.dom.Document.
        String svgNS = "http://www.w3.org/2000/svg";
        Document document = domImpl.createDocument(svgNS, "svg", null);
        // Create an instance of the SVG Generator.
        ig2 = new SVGGraphics2D(document);
        //ig2.getRoot(document.getDocumentElement());
        // }
        //else {
        //    ig2 = (SVGGraphics2D)bi.createGraphics();
        //}

        ig2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // make background white
        ig2.setPaint(Color.WHITE);
        ig2.fillRect(0, 0, pl.getPageWidth(), pl.getPageHeight());
        ig2.setPaint(Color.BLACK);

    //pl.drawAreaOutlines(ig2);
        // prepare font
        Font font = pl.getStandardFont();
        ig2.setFont(font);
        FontMetrics fontMetrics = ig2.getFontMetrics();

    // ------------------------- Draw header -------------------------
        // check width of header string
        String proteinHeader = "The chain complex graph of PDB entry " + cg.pdbid + " [V=" + cg.getVertices().size() + ", E=" + cg.getEdges().size() + "].";
        String addInfo = "(Interchain contact threshold is set to " + Settings.getInteger("plcc_I_cg_contact_threshold") + ". Neglected edges: " + cg.neglectedEdges + ")";
        //Integer stringWidth = fontMetrics.stringWidth(proteinHeader);       // Should be around 300px for the text above
        Integer stringHeight = fontMetrics.getAscent();
        String chainName;    // the SSE number in the primary structure, N to C terminus
        String chainNumber;  // the SSE number in this graph, 1..(this.size)

        if (Settings.getBoolean("plcc_B_graphimg_header")) {
            ig2.drawString(proteinHeader, pl.headerStart.x, pl.headerStart.y);
            //i2.drawString(addInfo, pl.headerStart.x, pl.headerStart.y + pl.textLineHeight);
        }

    // ------------------------- Draw the graph -------------------------
        // Draw the edges as arcs
        java.awt.Shape shape;
        Arc2D.Double arc;
        ig2.setStroke(new BasicStroke(2));  // thin edges
        Integer edgeType, leftVert, rightVert, leftVertPosX, rightVertPosX, arcWidth, arcHeight, arcTopLeftX, arcTopLeftY, spacerX, spacerY, iChainID, jChainID;
        Integer labelPosX, labelPosY;

        String edges = cg.getEdges().toString();
        for (Integer i = 0; i < cg.getVertices().size(); i++) {
            for (Integer j = i + 1; j < cg.getVertices().size(); j++) {

                String tmp = "(" + i + ", " + j + ")";

                // If there is a contact...
                if (edges.indexOf(tmp) != -1) {

                    Integer cInteractions = cg.numChainInteractions[i][j];

                // determine edge type and the resulting color
                    //edgeType = cg.getContactType(i, j);
                    ig2.setPaint(Color.GRAY);
                    if (bw) {
                        ig2.setPaint(Color.LIGHT_GRAY);
                    }      // for non-protein graphs

                // ----- complex graph specific stuff -----
                    // determine chain of SSEs
                    iChainID = -1;
                    jChainID = -1;
                    /*
                     for(Integer x = 0; x < cg.chainEnd.size(); x++){
                     if(i < cg.chainEnd.get(x)) {iChainID = x; break;}
                     }
                     for(Integer x = 0; x < cg.chainEnd.size(); x++){
                     if(j < cg.chainEnd.get(x)) {jChainID = x; break;}
                     }
                     if (!Objects.equals(iChainID, jChainID)) {ig2.setPaint(Color.PINK);}
                
                     */
                // ----- end complex graph specific stuff -----

                    // determine the center of the arc and the width of its rectangle bounding box
                    if (i < j) {
                        leftVert = i;
                        rightVert = j;
                    } else {
                        leftVert = j;
                        rightVert = i;
                    }

                    leftVertPosX = pl.getVertStart().x + (leftVert * pl.vertDist);
                    rightVertPosX = pl.getVertStart().x + (rightVert * pl.vertDist);

                    arcWidth = rightVertPosX - leftVertPosX;
                    arcHeight = arcWidth / 2;

                    arcTopLeftX = leftVertPosX;
                    arcTopLeftY = pl.getVertStart().y - arcHeight / 2;

                    spacerX = pl.vertRadius;
                    spacerY = 0;

                    // draw it                                                
                    arc = new Arc2D.Double(arcTopLeftX + spacerX, arcTopLeftY + spacerY, arcWidth, arcHeight, 0, 180, Arc2D.OPEN);
                    shape = ig2.getStroke().createStrokedShape(arc);
                    ig2.fill(shape);

                }
            }
        }

        // draw arc labels on top to prevent unreadability 
        for (Integer i = 0; i < cg.getVertices().size(); i++) {
            for (Integer j = i + 1; j < cg.getVertices().size(); j++) {

                String tmp = "(" + i + ", " + j + ")";

                // If there is a contact...
                if (edges.indexOf(tmp) != -1) {
                    iChainID = -1;
                    jChainID = -1;
                    if (i < j) {
                        leftVert = i;
                        rightVert = j;
                    } else {
                        leftVert = j;
                        rightVert = i;
                    }

                    // TODO: is it clever to calculate everything again?
                    leftVertPosX = pl.getVertStart().x + (leftVert * pl.vertDist);
                    rightVertPosX = pl.getVertStart().x + (rightVert * pl.vertDist);

                    arcWidth = rightVertPosX - leftVertPosX;
                    arcHeight = arcWidth / 2;

                    arcTopLeftX = leftVertPosX;
                    arcTopLeftY = pl.getVertStart().y - arcHeight / 2;

                    spacerX = pl.vertRadius;
                    spacerY = 0;
                    //calculate label positions
                    labelPosX = leftVertPosX + arcWidth / 2 + 2;
                    labelPosY = arcTopLeftY + spacerY - 5;
                    //draw labels on arcs
                    Font labelfont = new Font(Settings.get("plcc_S_img_default_font"), Font.PLAIN, Settings.getInteger("plcc_I_img_default_font_size") - 5);
                    ig2.setFont(labelfont);
                    ig2.setPaint(Color.BLACK);
                    Integer cInteractions = cg.numChainInteractions[i][j];
                    
                    if(cInteractions != null) {
                        ig2.drawString(cInteractions.toString(), labelPosX, labelPosY + (stringHeight / 4));
                    } else {
                        // This may happen if not the top-right part of the numChainInteractions is filled (due to some different ordering in the chains)
                        DP.getInstance().w("ComplexGraph", "Tried to read out a null entry from numChainInteractions while writing the edge weights in the chain-level CG. "
                                + "This is probably a programming error, so please inform the developers.");
                    }

                }
            }
        }

        ig2.setFont(font);
        Rectangle2D.Double rect;
        ig2.setStroke(new BasicStroke(2));
        
        Iterator<Vertex> vertIter = cg.getVertices().iterator();  // this and next line used to iterate vertices to get current vertex number for getUniqueHue
        Vertex curVert;
        
        for (Integer i = 0; i < cg.getVertices().size(); i++) {
            curVert = vertIter.next();

            // standard color: saturation and brightness will not be changed
            // set hue, saturation, brighness
            float h = (float) 0.5;
            float s = (float) 1.0; // change this for saturation (higher = more saturated)
            float b = (float) 0.8; // change this for brightness (0.0 -> Dark/Black)
            
            if (! bw) {
                ig2.setPaint(Color.getHSBColor(cg.getUniqueHue(Integer.parseInt(molInfoForChains.get(cg.proteinNodeMap.get(curVert)))), s, b));
            } else {
                ig2.setPaint(Color.GRAY);
            }
            
            // following code produces a color palette for m graphs containing n = 1..m vertices
            //   it is best to set min_width and min_height to 1200 and 900
            //   and turn header and footer off
            //   you can switch between 'old' getUniqueColor and 'new' getUniqueHue
            //   the result is saved as chain CG
            //   Important to note: the 'old' function is insensitive to the case of many nodes and few molIDs
            /*
            cg.molIDs.clear();
            for (Integer m = 1; m <= 80; m++) {
                System.out.println("m: " + m);
                cg.lastColorStep = 0;
                cg.molIDs.add(m.toString());
                for (int n = 1; n <= m; n++) {
                    rect = new Rectangle2D.Double(0 + n * 10, 0 + m * 10, 10, 10);
                    //ig2.setPaint(Color.getHSBColor(cg.getUniqueHue(n), s, b));
                    ig2.setPaint(Color.getHSBColor(cg.getUniqueColor(m), s, b));
                    ig2.fill(rect);
                }
            }
            i = 1000;  // no edges and vertices
            */
            
        // pick color depending on SSE type

            // draw a shape based on SSE type
            rect = new Rectangle2D.Double(vertStart.x + (i * pl.vertDist) + pl.getVertDiameter() / 2, vertStart.y - pl.getVertDiameter() / 2, pl.getVertDiameter(), pl.getVertDiameter());
            AffineTransform rot_45deg = new AffineTransform();
            rot_45deg.rotate(0.785, vertStart.x + (i * pl.vertDist) + pl.getVertDiameter() / 2, vertStart.y - pl.getVertDiameter() / 2); // rotation around center of vertex
            ig2.fill(rot_45deg.createTransformedShape(rect));

        }

        // Draw the markers for the N-terminus and C-terminus if there are any vertices in this graph            
        ig2.setStroke(new BasicStroke(2));
        ig2.setPaint(Color.BLACK);

        /*
         if( ! bw) {
         if(cg.getVertices().size() > 0) {                    
         ig2.drawString("N", vertStart.x - pl.vertDist, vertStart.y + 20);    // N terminus label
         ig2.drawString("C", vertStart.x + cg.getVertices().size() * pl.vertDist, vertStart.y + 20);  // C terminus label
         }
         }
         */
    // ************************************* footer **************************************
        if (Settings.getBoolean("plcc_B_graphimg_footer")) {

        // Draw the vertex numbering into the footer
        
            // old version where in CGs > 99 vertices only each second footer information is printed
            /*
            // Determine the dist between vertices that will have their vertex number printed below them in the footer field
            Integer printNth = 1;
            if (cg.getVertices().size() > 9) {
                printNth = 1;
            }
            if (cg.getVertices().size() > 99) {
                printNth = 2;
            }
            */

            // line markers: S for sequence order, G for graph order
            Integer lineHeight = pl.textLineHeight;
            if (cg.getVertices().size() > 0) {
                ig2.drawString("C#", pl.getFooterStart().x - pl.vertDist, pl.getFooterStart().y + (stringHeight / 4));
                ig2.drawString("CN", pl.getFooterStart().x - pl.vertDist, pl.getFooterStart().y + lineHeight + (stringHeight / 4));
                ig2.drawString("ML", pl.getFooterStart().x - pl.vertDist, pl.getFooterStart().y + (lineHeight *2) + (stringHeight / 4));
            } else {
                ig2.drawString("(Graph has no vertices.)", pl.getFooterStart().x, pl.getFooterStart().y);
            }
            iChainID = -1;
            String[] vertexNameAssignment = cg.proteinNodeMap.toString().replace("{", "").replace("}", "").replace(" ", "").split(",");  // produces array of "x=a" where x is number of vertex and a chain name
            
            
            
            for (Integer i = 0; i < cg.getVertices().size(); i++) {                
                // Draw each label until 999 and from then on only even ones
                if (i >= 999) {
                    if (i % 2 == 0) {
                        continue;
                    }
                }
                chainNumber = "" + (i + 1);
                //sseNumberSeq = "" + (cg.proteinNodeMap.get(i));

                // old version of getting chain name
                /*
                Integer foundIndex = edgesString.indexOf(i.toString() + "=");
                String chainId;
                if (i < 10) {
                    chainId = edgesString.substring(foundIndex + 2, foundIndex + 3);
                } else {
                    chainId = edgesString.substring(foundIndex + 3, foundIndex + 4);
                }
                chainName = "" + chainId;
                */

                chainName = vertexNameAssignment[i].split("=")[1];

                //stringWidth = fontMetrics.stringWidth(sseNumberSeq);
                stringHeight = fontMetrics.getAscent();

                ig2.drawString(chainNumber, pl.getFooterStart().x + (i * pl.vertDist) + pl.vertRadius / 2, pl.getFooterStart().y + (stringHeight / 4));
                ig2.drawString(chainName, pl.getFooterStart().x + (i * pl.vertDist) + pl.vertRadius / 2, pl.getFooterStart().y + (lineHeight * 1) + (stringHeight / 4));
                ig2.drawString(molInfoForChains.get(chainName), pl.getFooterStart().x + (i * pl.vertDist) + pl.vertRadius / 2, pl.getFooterStart().y + (lineHeight * 2) + (stringHeight / 4));

                // determine chain of SSEs
                /*for(Integer x = 0; x < cg.getVertices().size(); x++){
                 if(i < cg.chainEnd.get(x)) {iChainID = x; break;}
                 }
                 */
                //if(iChainID != -1) {ig2.drawString(cg.allChains.get(iChainID).getPdbChainID(), pl.getFooterStart().x + (i * pl.vertDist) + pl.vertRadius / 2, pl.getFooterStart().y + (lineHeight * 2) + (stringHeight / 4));}
            }

            /*
             if(Settings.getBoolean("plcc_B_graphimg_legend")) {
             if(iChainID != -1){
             SSEGraph.drawLegend(ig2, new Position2D(pl.getFooterStart().x, pl.getFooterStart().y + lineHeight * 3 + (stringHeight / 4)), pl, pg);
             }
             else{
             SSEGraph.drawLegend(ig2, new Position2D(pl.getFooterStart().x, pl.getFooterStart().y + lineHeight * 2 + (stringHeight / 4)), pl, pg);
             }
             }
             */
        }

    // all done, write the image to disk
        //if(Settings.get("plcc_S_img_output_format").equals("SVG")) {
    //boolean useCSS = true;
        //FileOutputStream fos = new FileOutputStream(new File("/tmp/mySVG.svg"));
        //Writer out = new OutputStreamWriter(fos, "UTF-8");
        //ig2.stream(out, useCSS); 
        Rectangle2D roi = new Rectangle2D.Double(0, 0, pl.getPageWidth(), pl.getPageHeight());

        DrawResult drawRes = new DrawResult(ig2, roi);
        return drawRes;

    }

    /**
     * Draw a complex graph to an image file in the requested format.
     *
     * @param baseFilePathNoExt the img base file name, no file extension
     * @param drawBlackAndWhite whether to draw in grayscale only
     * @param formats a list of img formats to write
     * @param cg the complex graph to draw
     * @param molInfoForChains info mapping chain IDs (like "A") to their macromolecule (MOL_ID in PDB file, e.g., "1"). Give an empty one if you dont know
     * @return a list of file names that were written to disk, (as a map of
     * formats to file names)
     */
    public static HashMap<DrawTools.IMAGEFORMAT, String> drawComplexGraph(String baseFilePathNoExt, Boolean drawBlackAndWhite, DrawTools.IMAGEFORMAT[] formats, ComplexGraph cg, Map<String, String> molInfoForChains) {

        DrawResult drawRes = ComplexGraph.drawChainLevelComplexGraphG2D(drawBlackAndWhite, cg, molInfoForChains);

        //System.out.println("drawProteinGraph: Basefilepath is '" + baseFilePathNoExt + "'.");
        String svgFilePath = baseFilePathNoExt + ".svg";
        HashMap<DrawTools.IMAGEFORMAT, String> resultFilesByFormat = new HashMap<DrawTools.IMAGEFORMAT, String>();
        try {
            DrawTools.writeG2dToSVGFile(svgFilePath, drawRes);
            resultFilesByFormat.put(DrawTools.IMAGEFORMAT.SVG, svgFilePath);
            resultFilesByFormat.putAll(DrawTools.convertSVGFileToOtherFormats(svgFilePath, baseFilePathNoExt, drawRes, formats));
        } catch (IOException ex) {
            DP.getInstance().e("Could not write protein graph file : '" + ex.getMessage() + "'.");
        }

        if (!Settings.getBoolean("plcc_B_silent")) {
            StringBuilder sb = new StringBuilder();
            sb.append("      Output complex graph files: ");
            for (DrawTools.IMAGEFORMAT format : resultFilesByFormat.keySet()) {
                String ffile = new File(resultFilesByFormat.get(format)).getName();
                sb.append("(").append(format.toString()).append(" => ").append(ffile).append(") ");
            }
            System.out.println(sb.toString());
        }
        return resultFilesByFormat;
    }

    /**
     * Writes this complex graph to the file 'file' in GML format. Note that
     * this function will overwrite the file if it exists.
     *
     * @param file the target file. Has to be writable.
     * @return true if the file was written, false otherwise
     */
    public boolean writeToFileGML(File file) {

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException ex) {
                System.err.println("ERROR: Could not create file '" + file.getAbsolutePath() + "': " + ex.getMessage() + ".");
                return false;
            }
        }

        GMLWriter<ComplexGraph.Vertex, ComplexGraph.Edge> gw = new GMLWriter<ComplexGraph.Vertex, ComplexGraph.Edge>(this);        
        gw.addVertexAttrWriter(new GMLWriter.AttrWriter<Vertex>() {
            @Override
            public String getAttribute() {
                return "label";
            }

            @Override
            public boolean hasValue(Vertex o) {
                return proteinNodeMap.containsKey(o);
            }

            @Override
            public String write(Vertex o) {
                return '"' + proteinNodeMap.get(o).toString() + '"';
            }
        });
        
        // add a new line per node holding the chain's molecule name
        gw.addVertexAttrWriter(new GMLWriter.AttrWriter<Vertex>() {
            @Override
            public String getAttribute() {
                return "mol_name";
            }

            @Override
            public boolean hasValue(Vertex o) {
                return molMap.containsKey(o);
            }

            @Override
            public String write(Vertex o) {
                return '"' + molMap.get(o) + '"';
            }
        });

        //gw.addEdgeAttrWriter(new GMLWriter.MapAttrWriter<>());
        /**
         * Overwrite the edge attribute writer for the labels. This has to be
         * done because the labels need to be enclosed in closing quotation
         * marks to mark them as strings (due to the GML format definition).
         */
        gw.addEdgeAttrWriter(new GMLWriter.AttrWriter<Edge>() {
            @Override
            public String getAttribute() {
                return "label";
            }

            @Override
            public boolean hasValue(Edge e) {
                return numAllInteractionsMap.containsKey(e);
            }

            @Override
            public String write(Edge e) {
                return '"' + numAllInteractionsMap.get(e).toString() + '"';
            }

        });
       
        gw.addEdgeAttrWriter(new GMLWriter.MapAttrWriter<>("num_helixhelix_contacts", numHelixHelixInteractionsMap));
        gw.addEdgeAttrWriter(new GMLWriter.MapAttrWriter<>("num_helixstrand_contacts", numHelixStrandInteractionsMap));
        gw.addEdgeAttrWriter(new GMLWriter.MapAttrWriter<>("num_helixligand_contacts", numHelixLigandInteractionsMap));
        gw.addEdgeAttrWriter(new GMLWriter.MapAttrWriter<>("num_helixcoil_contacts", numHelixCoilInteractionsMap));
        gw.addEdgeAttrWriter(new GMLWriter.MapAttrWriter<>("num_strandstrand_contacts", numStrandStrandInteractionsMap));
        gw.addEdgeAttrWriter(new GMLWriter.MapAttrWriter<>("num_strandcoil_contacts", numStrandCoilInteractionsMap));
        gw.addEdgeAttrWriter(new GMLWriter.MapAttrWriter<>("num_strandligand_contacts", numStrandLigandInteractionsMap));
        gw.addEdgeAttrWriter(new GMLWriter.MapAttrWriter<>("num_coilcoil_contacts", numCoilCoilInteractionsMap));
        gw.addEdgeAttrWriter(new GMLWriter.MapAttrWriter<>("num_coilligand_contacts", numCoilLigandInteractionsMap));

        FileOutputStream fop = null;
        boolean allOK = true;
        try {
            fop = new FileOutputStream(file);
            gw.write(fop);
            fop.flush();
            fop.close();
        } catch (Exception e) {
            System.err.println("ERROR: Could not write complex graph to file '" + file.getAbsolutePath() + "': " + e.getMessage() + ".");
            allOK = false;
        } finally {
            if (fop != null) {
                try {
                    fop.close();
                } catch (Exception e) {
                    // nvm
                }
            }

        }
        return allOK;
    }
    
    
     public ArrayList<String> getContactInfo() {
        if (createContactInfo) {
            return contactInfo;
        }
        else {
            DP.getInstance().w("Tried to get CG's contact info despite setting plcc_B_writeComplexContactCSV was off. Returning null.");
            return null;
        }
    }
}
