from pyAudioAnalysis import audioTrainTest as aT
#aT.featureAndTrain(["/home/tyiannak/Desktop/MusicGenre/Classical/","/home/tyiannak/Desktop/MusicGenre/Electronic/","/home/tyiannak/Desktop/MusicGenre/Jazz/"], 1.0, 1.0, aT.shortTermWindow, aT.shortTermStep, "svm", "svmMusicGenre3", True)
aT.featureAndTrain(["pyAudioAnalysis/data/bones_music/","pyAudioAnalysis/data/speech_vids/"], 1.0, 1.0, aT.shortTermWindow, aT.shortTermStep, "svm", "svmBones", False)
print aT.fileClassification("Intro_obtained_from_longest_common_subsequence.wav", "svmBones","svm")