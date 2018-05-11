from pydub import AudioSegment
import json
import librosa
from dtw import dtw
import numpy as np
import subprocess

class DetectIntro:
    def __init__(self, path_to_song):
        full_song = AudioSegment.from_wav(path_to_song)
        # Clip first 5 seconds of song
        intro_song = full_song[:5000]
        intro_song.export("intro.wav", format="wav")
        # Clip last 5 seconds of song
        exit_song = full_song[-5000:]
        exit_song.export("exit.wav", format="wav")
        
        self.path_to_intro_wav = "intro.wav"
        self.path_to_exit_wav = "exit.wav"
    
    def input_episode(self, path_to_episode_mp4):
        command = "ffmpeg -i " + path_to_episode_mp4 + " -ab 160k -ac 2 -ar 44100 -vn " + path_to_episode_mp4[:-4] +".wav"
        self.path_to_episode_wav = path_to_episode_mp4[:-4] +".wav"
        l = subprocess.call(command, shell=True)
        self.episode = AudioSegment.from_wav(self.path_to_episode_wav)
        self.episode_name = path_to_episode_mp4[:-4]

    def detect(self, segment_start, segment_end):
        final_end = len(self.episode)
        segment_to_analyze = self.episode
        y1, sr1 = librosa.load(self.path_to_intro_wav)
        start_found = False
        start_time = 0
        end_time = 0
        data = {}
        data['name'] = self.episode_name
        data['metadata'] = {}
        while segment_end < final_end:
            chunk = segment_to_analyze[segment_start:segment_end]
            chunk.export("chunk.wav",format="wav")
            
            y2, sr2 = librosa.load("chunk.wav")
            
            # MFCC values to compare distance 
            mfcc1 = librosa.feature.mfcc(y1, sr1)   
            mfcc2 = librosa.feature.mfcc(y2, sr2) 
            dist, _, _, _ = dtw(mfcc1.T, mfcc2.T,dist=lambda x, y: np.linalg.norm(x - y, ord=1))
            
            # Chroma values to compare distance 
            chroma1 = librosa.feature.chroma_cens(y=y1, sr=sr1)
            chroma2 = librosa.feature.chroma_cens(y=y2, sr=sr2)
            dist1, _, _, _ = dtw(chroma1.T, chroma2.T,dist=lambda x, y: np.linalg.norm(x - y, ord=1))
            
            print("Time is :",segment_start," to ",segment_end)
            # 0 for similar audios
            print("The normalized distance between the two : ",dist)    
            
            # Intro was detected
            if not start_found and (dist1 < 0.45 or dist < 130):
                chunk.export("identified_intro.wav",format="wav")
                start_found = True
                start_time = segment_start
                y1, sr1 = librosa.load(self.path_to_exit_wav)
                print('---------------------start found------------------------')
            
            elif start_found and (dist1 < 0.45 or dist < 130):
                chunk.export("identified_exit.wav",format="wav")
                end_time = segment_end
                print('---------------------exit found------------------------')
                break
            
            segment_start = segment_start + 1000
            segment_end = segment_end + 1000
        
        data['metadata']['startTime'] = start_time/1000
        data['metadata']['endTime'] = end_time/1000
        
        ep_data = json.dumps(data)
        with open(self.episode_name +"_data.json","w") as f:
            f.write(ep_data)
            
        return data

# Enter song path
# the_office = DetectIntro("TheOfficeIntroSong.wav")

# Enter episode path
# the_office.input_episode('Office_episode1.mp4')
# ep_data = the_office.detect(0, 5000)