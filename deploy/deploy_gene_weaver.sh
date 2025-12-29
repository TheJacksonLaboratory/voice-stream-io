# CURRENTLY IT IS BETTER NOT TO DEPLOY THIS FUNCTION AND TO 
# PARTITION, WRITE THE ZIP FILES AND UPLOAD THE BUCKETS LOCALLY.

# Things to do before running this (these need to be automated).
# 1. Create a repo bitbucket_geneweaver_variant-orthology-io referencing git@bitbucket.org:geneweaver/variant-orthology-io.git in google.
# 2. The bucket must exist. In this case we call them 'variant-orthology-partition-bucket' and 'variant-orthology-upload-bucket'. 
# 3. Make sure that yor gcloud commands are pointing to the correct project first, 'gcloud init'

# @see src/main/java/org/jax/gweaver/variant/orthology/function/FilePartitionFunction.java

gcloud functions deploy file-partition-function \
--region us-east1 \
--timeout 540 \
--source https://source.developers.google.com/projects/jax-geneweaver-dev-nc-01/repos/bitbucket_geneweaver_variant-orthology-io \
--entry-point org.jax.voice.variant.orthology.function.FilePartitionFunction \
--update-env-vars PARITION_BUCKET=variant-orthology-partition-bucket \
--runtime java11 \
--memory 1024MB \
--trigger-resource variant-orthology-upload-bucket \
--trigger-event google.storage.object.finalize