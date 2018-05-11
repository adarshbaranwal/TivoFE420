from pydub import AudioSegment
import librosa
import librosa.display
import matplotlib.pyplot as plt
from fastdtw import fastdtw
import numpy as np
from scipy.spatial.distance import euclidean
import argparse
import sys

def generate_intro_clip(filename1,filename2,intro_clip_name):
    song1 = AudioSegment.from_wav(filename1)
    song2 = AudioSegment.from_wav(filename2)
    segment_to_analyze1=song1[0:480000]
    segment_to_analyze2=song2[0:480000]
    segment_start1=1
    segment_end1=5000
    final_end1=480000
    segment_start2=1
    segment_end2=5000
    final_end2=480000
    song1_mfcc=[]
    song2_mfcc=[]
    lvar1=0
    while segment_end1 <= final_end1:
        #segment_to_analyze=song[0:600000]
        chunk1=segment_to_analyze1[segment_start1:segment_end1]
        chunk1.export("chunk_seg1.wav",format="wav")
        y1, sr1 = librosa.load('chunk_seg1.wav')
        song1_mfcc.append(librosa.feature.mfcc(y1,sr1))
        lvar1=lvar1+1
        segment_start1=segment_start1+5000
        segment_end1=segment_end1+5000
    #print "mfcc song 1=",song1_mfcc
    print len(song1_mfcc)

    lvar2=0
    while segment_end2 <= final_end2:
        #segment_to_analyze=song[0:600000]
        chunk2=segment_to_analyze2[segment_start2:segment_end2]
        chunk2.export("chunk_seg2.wav",format="wav")
        y2, sr2 = librosa.load('chunk_seg2.wav')
        song2_mfcc.append(librosa.feature.mfcc(y2,sr2))
        lvar2=lvar2+1
        segment_start2=segment_start2+5000
        segment_end2=segment_end2+5000

    #print "mfcc song 2=",song2_mfcc
    print len(song2_mfcc)

    L = [[0 for x in xrange(len(song2_mfcc))] for x in xrange(len(song1_mfcc))]
    distarr = [[0 for x in xrange(len(song2_mfcc))] for x in xrange(len(song1_mfcc))]
    print len(L)
    print len(L[0])

    for i in xrange(len(song1_mfcc)):
        for j in xrange(len(song2_mfcc)):
            dist, path = fastdtw(song1_mfcc[i].T, song2_mfcc[j].T,dist=euclidean)
            distarr[i][j]=dist
            print "dist is",dist
            if i == 0 or j == 0:
                L[i][j] = 0
            elif dist < 12000:
                L[i][j] = L[i-1][j-1] + 1
            else:
                L[i][j] = max(L[i-1][j], L[i][j-1])

    index=L[len(song1_mfcc)-1][len(song2_mfcc)-1]
    print index
    print "Completed stage 1"
    lcs=[0]*index
    i = len(song1_mfcc)-1
    j = len(song2_mfcc)-1
    while i > 0 and j > 0:
        # If current character in X[] and Y are same, then
        # current character is part of LCS
        #dist, cost, acc_cost,path = dtw(song1_mfcc[i].T, song2_mfcc[j].T,dist=lambda x, y: np.linalg.norm(x - y, ord=1))
        if distarr[i][j] < 12000:
            lcs[index-1] = (2*(i+1)-1)*2500
            i-=1
            j-=1
            index-=1
     
        # If not same, then find the larger of two and
        # go in the direction of larger value
        elif L[i-1][j] > L[i][j-1]:
            i-=1
        else:
            j-=1

    print "lcs is:",lcs
    chunkstart=lcs[0]-2500
    chunkend=lcs[-1]+2500
    finalchunk=song1[chunkstart:chunkend]
    finalchunk.export(intro_clip_name,format="wav")
    print "The file "+intro_clip_name+" has been created"

def main():
    parser = argparse.ArgumentParser(
                                     description="Tool for generating intro clip")

    parser.add_argument("--filename1",
                        metavar='FILENAME1',
                        required = True,
                        help="the first file")
    parser.add_argument("--filename2",
                        metavar='FILENAME2',
                        required = True,
                        help="the second file")
    parser.add_argument("--intro_clip_name",
                        metavar='INTRO_CLIP_NAME',
                        required = True,
                        help="the name with which the intro file should be created")
    
    args = parser.parse_args()
    generate_intro_clip(args.filename1,args.filename2,args.intro_clip_name)
if __name__ == "__main__":
    try:
        main()
        print("Process Completed Successfully!!!!!")
    except Exception as e:
        print("Exception running script: ", str(e))
        sys.exit(1)
