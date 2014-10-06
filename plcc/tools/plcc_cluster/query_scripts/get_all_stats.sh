#!/bin/sh


for SCRIPT in q_overview.sh q_sselengths.sh q_functions.sh q_contacts.sh q_contact_details.sh q_organisms.sh q_graphtypes.sh q_motifs.sh
do
	echo "Handling script '$SCRIPT'..."
	./$SCRIPT > ./results/result_${SCRIPT}
done


echo "Done."
exit 0

