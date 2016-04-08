graph [
  id 1
  label "VPLG Protein Graph 3lnd-A-alphalig[6,3]"
  comment "date=02-FEB-10|keywords=CADHERIN, CELL ADHESION, CELL MEMBRANE, MEMBRANE, TRANSMEMBRANE|graphclass=protein graph|pdb_org_common=MOUSE|title=CRYSTAL STRUCTURE OF CADHERIN-6 EC12 W4A|resolution=2.82|pdb_all_chains=A, B, C, D|pdb_mol_id=1|pdb_mol_name=CDH6 PROTEIN|pdbid=3lnd|graphtype=alphalig|experiment=X-RAY DIFFRACTION|chainid=A|pdb_org_sci=MUS MUSCULUS|pdb_ec_number=|header=CELL ADHESION|"
  directed 0
  isplanar 0
  creator "PLCC version 0.98.1"
  pdb_id "3lnd"
  chain_id "A"
  graph_type "alphalig"
  is_protein_graph 1
  is_folding_graph 0
  is_SSE_graph 1
  is_AA_graph 0
  is_all_chains_graph 0
  node [
    id 0
    label "0-H"
    num_in_chain 5
    num_residues 3
    pdb_res_start "A-65- "
    pdb_res_end "A-67- "
    dssp_res_start 61
    dssp_res_end 63
    pdb_residues_full "A-65- ,A-66- ,A-67- "
    aa_sequence "REE"
    sse_type "H"
    fg_notation_label "h"
  ]
  node [
    id 1
    label "1-H"
    num_in_chain 11
    num_residues 3
    pdb_res_start "A-152- "
    pdb_res_end "A-154- "
    dssp_res_start 145
    dssp_res_end 147
    pdb_residues_full "A-152- ,A-153- ,A-154- "
    aa_sequence "QPY"
    sse_type "H"
    fg_notation_label "h"
  ]
  node [
    id 2
    label "2-H"
    num_in_chain 15
    num_residues 3
    pdb_res_start "A-188- "
    pdb_res_end "A-190- "
    dssp_res_start 180
    dssp_res_end 182
    pdb_residues_full "A-188- ,A-189- ,A-190- "
    aa_sequence "MGG"
    sse_type "H"
    fg_notation_label "h"
  ]
  node [
    id 3
    label "3-L"
    num_in_chain 17
    num_residues 1
    pdb_res_start "A-208- "
    pdb_res_end "A-208- "
    dssp_res_start 809
    dssp_res_end 809
    pdb_residues_full "A-208- "
    aa_sequence "J"
    lig_name " CA"
    sse_type "L"
    fg_notation_label "l"
  ]
  node [
    id 4
    label "4-L"
    num_in_chain 18
    num_residues 1
    pdb_res_start "A-209- "
    pdb_res_end "A-209- "
    dssp_res_start 810
    dssp_res_end 810
    pdb_residues_full "A-209- "
    aa_sequence "J"
    lig_name " CA"
    sse_type "L"
    fg_notation_label "l"
  ]
  node [
    id 5
    label "5-L"
    num_in_chain 19
    num_residues 1
    pdb_res_start "A-210- "
    pdb_res_end "A-210- "
    dssp_res_start 811
    dssp_res_end 811
    pdb_residues_full "A-210- "
    aa_sequence "J"
    lig_name " CA"
    sse_type "L"
    fg_notation_label "l"
  ]
  edge [
    source 0
    target 4
    label "j"
    spatial "j"
  ]
  edge [
    source 0
    target 5
    label "j"
    spatial "j"
  ]
  edge [
    source 4
    target 5
    label "j"
    spatial "j"
  ]
]
