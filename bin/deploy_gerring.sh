# CURRENTLY IT IS BETTER NOT TO DEPLOY THIS FUNCTION AND TO 
# PARTITION, WRITE THE ZIP FILES AND UPLOAD THE BUCKETS LOCALLY.

# Things to do before running this (these need to be automated).
# 1. Create a repo bitbucket_geneweaver_variant-orthology-io referencing git@bitbucket.org:geneweaver/variant-orthology-io.git in google.
# 2. The bucket must exist. In this case we call them 'variant-orthology-partition-bucket-test' and 'variant-orthology-upload-bucket-test'. 
# 3. Make sure that yor gcloud commands are pointing to the correct project first, 'gcloud init'

# @see src/main/java/org/jax/gweaver/variant/orthology/function/FilePartitionFunction.java

gsutil mb -l us-east1 gs://variant-orthology-upload-bucket-test

gcloud functions deploy file-partition-function \
--region us-east1 \
--timeout 540 \
--source https://source.developers.google.com/projects/geneweaver-test-orthology/repos/bitbucket_geneweaver_variant-orthology-io \
--entry-point org.jax.gweaver.variant.orthology.function.FilePartitionFunction \
--update-env-vars PARITION_BUCKET=variant-orthology-partition-bucket-test \
--runtime java11 \
--memory 2048MB \
--trigger-resource variant-orthology-upload-bucket-test \
--trigger-event google.storage.object.finalize