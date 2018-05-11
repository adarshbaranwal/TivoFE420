from pydub import AudioSegment
import librosa
import librosa.display
import matplotlib.pyplot as plt
from dtw import dtw
import numpy as np
from pyAudioAnalysis import audioTrainTest as aT
import sys
import argparse
import json

def get_intro_skip_timeline(filename):
    song = AudioSegment.from_wav(filename)
    segment_start=0
    segment_end=5000
    final_end=600000
    segment_to_analyze=song[0:600000]
    #y1, sr1 = librosa.load('intro.wav')
    speech_count=1
    prev_music=False
    segment_arr=[]
    subchunkarr=[]
    while segment_end < final_end:
        chunk=segment_to_analyze[segment_start:segment_end]
        chunk.export("chunk_test.wav",format="wav")
        #y2, sr2 = librosa.load("chunk.wav")
        Result,P,classNames= aT.fileClassification("chunk_test.wav", "svmBones","svm")
        if P[0]>0.85:
            print "Time is :",segment_start," to ",segment_end
            print P[0],classNames[0]
            subchunkarr.append([segment_start,segment_end])
            prev_music=True
        else:
            if prev_music==True:
                segment_arr.append(subchunkarr)
                subchunkarr=[]
                prev_music=False        
            #if prev_music == True:
        #else:
        #    print classNames[1]
        #plt.subplot(1, 2, 1) 
        #mfcc1 = librosa.feature.mfcc(y1,sr1)   #Computing MFCC values
        #librosa.display.specshow(mfcc1)plt.subplot(1, 2, 2)
        #mfcc2 = librosa.feature.mfcc(y2, sr2)
        #librosa.display.specshow(mfcc2)
        #dist, cost, acc_cost,path = dtw(mfcc1.T, mfcc2.T,dist=lambda x, y: np.linalg.norm(x - y, ord=1))
        #print("The normalized distance between the two : ",dist)   # 0 for similar audios
        #if dist<100:
        #    chunk.export("identifiedintro.wav",format="wav")
        #else:
        #    chunk.export("speech_vids/speechfile_"+str(speech_count)+".wav",format="wav")
        #    speech_count=speech_count+1
        segment_start=segment_start+2500
        segment_end=segment_end+2500
    if prev_music==True:
        segment_arr.append(subchunkarr)
    print segment_arr
    i=0
    maxlen=0
    maxind=0
    while i<len(segment_arr):
        if len(segment_arr[i])>maxlen:
            maxlen=len(segment_arr[i])
            maxind=i
        i+=1
    print "Max index is:",maxind
    print "Array is:",segment_arr[maxind]
    print "Intro chunk is from",segment_arr[maxind][0][0]," to ",segment_arr[maxind][-1][1]
    intro_chunk=song[segment_arr[maxind][0][0]:segment_arr[maxind][-1][1]]
    fileDetails={}
    skipIntro={}
    fileDetails["fileName"]=filename
    skipIntro["startTime"]=str(segment_arr[maxind][0][0]/1000)
    skipIntro["endTime"]=str(segment_arr[maxind][-1][1]/1000)
    fileDetails["metadata"]=skipIntro
    print json.dumps(fileDetails)
    intro_chunk.export("skip_clip2.wav",format="wav")

#first_10_seconds = song[282000:287000]
#first_10_seconds.export("intro_not_there.wav",format="wav")

def main():
    parser = argparse.ArgumentParser(
                                     description="Tool for generating intro clip")

    parser.add_argument("--filename",
                        metavar='FILENAME',
                        required = True,
                        help="the file name")
    args = parser.parse_args()
    get_intro_skip_timeline(args.filename)
if __name__ == "__main__":
    try:
        main()
        print("Process Completed Successfully!!!!!")
    except Exception as e:
        print("Exception running script: ", str(e))
        sys.exit(1)