from CNNTrainTest import cnn_TrainTest

# run 10 class on Spectrograms
#cnn_TrainTest(50,2,64,32,'EmoFract','no_of_classes_10','/home/gpu/Documents/Data/RunTests/' )
#cnn_TrainTest(100,2,32,32,'EmoFract','no_of_classes_7','/home/gpu/Documents/Data/RunTests/',"",1,1024,64 )
#cnn_TrainTest(100,2,64,64,'EmoFract','no_of_classes_7','/home/gpu/Documents/Data/RunTests/',"",1,1024,64)
#cnn_TrainTest(100,2,128,128,'EmoFract','no_of_classes_7','/home/gpu/Documents/Data/RunTests/',"",1,1024,64)


#cnn_TrainTest(50,2,64,32,'Spectr_full','no_of_classes_10','/home/gpu/Documents/Data/RunTests/' )
#cnn_TrainTest(100,2,64,32,'Spectr_full','no_of_classes_70','/home/gpu/Documents/Data/RunTests/' )
#cnn_TrainTest(100,2,128,64,'Spectr_full','no_of_classes_70','/home/gpu/Documents/Data/RunTests/' )
#cnn_TrainTest(100,2,128,128,'Spectr_full','no_of_classes_70','/home/gpu/Documents/Data/RunTests/' )


cnn_TrainTest(50,2,64,32,'Cepstr_full','no_of_classes_10','/home/gpu/Documents/Data/RunTests/' )
cnn_TrainTest(100,2,64,32,'Cepstr_full','no_of_classes_70','/home/gpu/Documents/Data/RunTests/' )
cnn_TrainTest(100,2,128,64,'Cepstr_full','no_of_classes_70','/home/gpu/Documents/Data/RunTests/' )
cnn_TrainTest(100,2,128,128,'Cepstr_full','no_of_classes_70','/home/gpu/Documents/Data/RunTests/' )

#cnn_TrainTest(50,2,64,32,'Chroma20bin','no_of_classes_10','/home/gpu/Documents/Data/RunTests/' )
#cnn_TrainTest(100,2,64,32,'Chroma20bin','no_of_classes_70','/home/gpu/Documents/Data/RunTests/' )
#cnn_TrainTest(100,2,128,64,'Chroma20bin','no_of_classes_70','/home/gpu/Documents/Data/RunTests/' )
#cnn_TrainTest(100,2,128,128,'Chroma20bin','no_of_classes_70','/home/gpu/Documents/Data/RunTests/' )

#cnn_TrainTest(50,2,64,32,'Chroma','no_of_classes_10','/home/gpu/Documents/Data/RunTests/')
#cnn_TrainTest(100,2,64,32,'Chroma','no_of_classes_70','/home/gpu/Documents/Data/RunTests/')
#cnn_TrainTest(100,2,128,64,'Chroma','no_of_classes_70','/home/gpu/Documents/Data/RunTests/')
#cnn_TrainTest(100,2,128,128,'Chroma','no_of_classes_70','/home/gpu/Documents/Data/RunTests/')

#cnn_TrainTest(50,2,64,32,'Chroma','no_of_classes_10','/home/gpu/Documents/Data/RunTests/','#0')
#cnn_TrainTest(100,2,64,32,'Chroma','no_of_classes_70','/home/gpu/Documents/Data/RunTests/')
#cnn_TrainTest(100,2,128,64,'Chroma','no_of_classes_70','/home/gpu/Documents/Data/RunTests/')
#cnn_TrainTest(100,2,128,128,'Chroma','no_of_classes_70','/home/gpu/Documents/Data/RunTests/')
