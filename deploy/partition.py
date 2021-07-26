import os
from os import listdir
from os.path import isfile, join
import shutil
import pathlib

import zipfile
from zipfile import ZipFile

from google.cloud import storage


class Partitioner(object):
    '''
    This class is deprecated. It does not partition gene files in
    whole gene increments. Instead use the Java alternative in this
    project. We keep the script here for inspiration on partitioning.
    '''
    
    
    def upload(self, dir, uploadbucket="variant-orthology-upload-bucket-test"):
        ''' Just do the upload '''
        files = [f for f in listdir(dir) if isfile(join(dir, f))]
        os.chdir(dir)
        try:
            for file in files:
                self._upload(file, uploadbucket)
        finally:
            os.chdir("../")

    
    def partition(self, bigfile, size, dir, uploadbucket="variant-orthology-upload-bucket-test"):
        '''
        Process files and uploads them.
        '''
    
        shutil.rmtree(dir, ignore_errors=True)
        os.makedirs(dir)
        self._partition(bigfile, size, dir, uploadbucket)
    
    def _partition(self, bigpath, size, dir, uploadbucket):
        '''
        Partition a big file by lines to
        a directory
        '''
        fileNumber = 0
        bigName, bigExt = os.path.splitext(bigpath)
        
        smallpath = None
        with open(bigpath) as bfile:
            
            smallfile = None
            
            for lineno, line in enumerate(bfile):
                if lineno % size == 0:
                    if smallfile:
                        smallfile.close()
                        self._send(smallpath, uploadbucket)
                        
                    smallpath = '{}/{}_{}{}'.format(str(dir), bigName, fileNumber, bigExt)
                    smallfile = open(smallpath, "w")
                    fileNumber+=1
                    
                smallfile.write(line)
                
            if smallfile:
                smallfile.close()
                self._send(smallpath, uploadbucket)
                
    def _send(self, smallpath, uploadbucket):
        
        fpath = pathlib.Path(smallpath)
        dir = fpath.parent
        os.chdir(dir)
        try:
            zpath = self._compress(fpath.name)
            self._upload(zpath, uploadbucket)
        finally:
            os.chdir("../")
       
    def _compress(self, path):
        '''
        Zips each path and deletes the unzipped partition
        in order to save space.
        '''
        
        zpath = "{}.zip".format(path)
    
        with ZipFile(zpath, 'w', zipfile.ZIP_DEFLATED) as zfile:
            zfile.write(path)
            print("Written {}".format(zpath))
        
        os.remove(path)
        return zpath
            
    def _upload(self, zpath, bucket):
        
        '''
        In order for this to work you must have installed gcp CLI and done
        a gcloud init to ensure that the correct project is active.
        @see https://riptutorial.com/google-cloud-storage/example/28256/upload-files-using-python
        '''
        client = storage.Client()
        bucket = client.get_bucket(bucket)
        blob = bucket.blob(zpath)
        
        with open(zpath, 'rb') as file:
            blob.upload_from_file(file)

        print("Uploaded {}".format(zpath))
        os.remove(zpath)


if __name__ == "__main__":
    
    lines_per_file = 1000000
    wkdir = '/Users/gerrim/JAX/data/variant-orthology' # There is not space on my machine for this, it is a linked external drive.

    # You can change to where the 'download_data.sh' downloaded the files.
    # This might be a shared disk or external drive.
    os.chdir(wkdir)
    
    # Credentials for your user / project, download at https://console.cloud.google.com/apis/credentials
    os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = wkdir+"/Geneweaver Test Orthology-8c6f6e417340.json"
    
    # Process the large files into partitions, zip them and then upload them.
    partitioner = Partitioner()
    #partitioner.process("Homo_sapiens.GRCh38.91.gtf", lines_per_file, "hs_gtf")
    #partitioner.process("Mus_musculus.GRCm38.91.gtf", lines_per_file, "mm_gtf")
    partitioner.partition("homo_sapiens_incl_consequences.gvf", lines_per_file, "hs_gvf")
    partitioner.partition("mus_musculus_incl_consequences.gvf", lines_per_file, "mm_gvf")
         