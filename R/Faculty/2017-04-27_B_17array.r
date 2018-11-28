####################################################
# Talon cluster
# Plan B - Corpus merged between CSR dates (i.e., Original Plan)
# Using Array for different k (number of topics)
#
# Based on:
# 2016-11-09_talon.r AND 2016-12-02_talon.r
# Topic models (Latent Dirichlet Allocation)
# LDA model for data 2014-09-02
# Determine optimal number of k topics
# Notes: 1) Fix the line of code about of saving the variable 'abc'
#        2) We now do 1000 iterations to
#           narrow the 95% confident intervals
#
# NO LOOP
# FOLDER FOR OUTPUTS: EN1_CDC_LDA3
#
######################################################
# Major change:
# Attempt to run in array
#
####################################################################

jobID=Sys.getenv("SLURM_ARRAY_TASK_ID") #get the array id from UNIX script

#jobID=1


setwd("/work/mperkins/cfung")
set.seed(32)
require(stringr)
require(tm)
require(e1071)
require(SnowballC)
require(topicmodels)
require(psych)
require(ggplot2)
# R function provided by Mr. Chung-Hong Chan, University of Hong Kong.
# Date: March 21, 2016
# Function to convert upper case to lower case
save_tolower <- function(x) {
  return(chartr("ABCDEFGHIJKLMNOPQRSTUVWXYZ",
                "abcdefghijklmnopqrstuvwxyz", x))
}

###
# List of dates that we wanted to study and compare with CSR
vec0 <- c("2014-09-01", #1
"2014-09-02", #2
"2014-09-04", #3
"2014-09-07", #4
"2014-09-09", #5
"2014-09-11", #6
"2014-09-14", #7
"2014-09-16", #8
"2014-09-18", #9
"2014-09-21", #10
"2014-09-23", #11
"2014-09-25", #12
"2014-09-28", #13
"2014-09-30", #14
"2014-10-02", #15
"2014-10-05", #16
"2014-10-07", #17
"2014-10-09", #18
"2014-10-13", #19
"2014-10-14", #20
"2014-10-16", #21
"2014-10-19", #22
"2014-10-21", #23
"2014-10-23", #24
"2014-10-26", #25
"2014-10-28", #26
"2014-10-30") #27
length(vec0)
#[1] 27

i <- 17
placeholder <- "./EN1_CDC_LDA3_array/CDC_EN1_"
Filename01<-paste("./EN1_CDC/CDC_EN1_",
                  as.character(format(as.Date(vec0[i], origin="1970-01-01"), format="%Y-%m-%d")),
                  ".RDS",
                  sep="", collapse="")
Filename02<-paste("./EN1_CDC/CDC_EN1_",
                  as.character(format(as.Date(vec0[i], origin="1970-01-01")-1, format="%Y-%m-%d")),
                  ".RDS",
                  sep="", collapse="")

Filename03<-paste("./EN1_CDC/CDC_EN1_",
                  as.character(format(as.Date(vec0[i], origin="1970-01-01")-2, format="%Y-%m-%d")),
                  ".RDS",
                  sep="", collapse="")

Filename04<-paste("./EN1_CDC/CDC_EN1_",
                  as.character(format(as.Date(vec0[i], origin="1970-01-01")-3, format="%Y-%m-%d")),
                  ".RDS",
                  sep="", collapse="")


Filename_term_freq<-paste(placeholder,
                              as.character(format(as.Date(vec0[i], origin="1970-01-01"), format="%Y-%m-%d")), "_", jobID,
                              "_term_freq.csv",
                              sep="", collapse="")

Filename_Terms<-paste(placeholder,
                          as.character(format(as.Date(vec0[i], origin="1970-01-01"), format="%Y-%m-%d")),
                          "_LDA_Terms.csv",
                          sep="", collapse="")

Filename_Topics<-paste(placeholder,
                      as.character(format(as.Date(vec0[i], origin="1970-01-01"), format="%Y-%m-%d")),
                      "_LDA_Topics.csv",
                      sep="", collapse="")

Filename_TopicModels<-paste(placeholder,
                       as.character(format(as.Date(vec0[i], origin="1970-01-01"), format="%Y-%m-%d")),
                       "_TopicModels.RDS",
                       sep="", collapse="")

Filename_Perplex<-paste(placeholder,
                            as.character(format(as.Date(vec0[i], origin="1970-01-01"), format="%Y-%m-%d")), "_", jobID,
                            "_Perplex.csv",
                            sep="", collapse="")
### Added job ID to filename for Filename_Perplex






if(as.integer(as.Date(vec0[i], origin="1970-01-01")-as.Date(vec0[i-1], origin="1970-01-01"))==1){
  df <- readRDS(file=Filename01)

}
if(as.integer(as.Date(vec0[i], origin="1970-01-01")-as.Date(vec0[i-1], origin="1970-01-01"))==2){
  df <- readRDS(file=Filename01)
  df_2 <- readRDS(file=Filename02)
  df<-rbind(df_2,df)
  rm(df_2)
}
if(as.integer(as.Date(vec0[i], origin="1970-01-01")-as.Date(vec0[i-1], origin="1970-01-01"))==3){
  df <- readRDS(file=Filename01)
  df_2 <- readRDS(file=Filename02)
  df_3 <- readRDS(file=Filename03)
  df<-rbind(df_3,df_2,df)
  rm(df_2)
  rm(df_3)
}
if(as.integer(as.Date(vec0[i], origin="1970-01-01")-as.Date(vec0[i-1], origin="1970-01-01"))==4){
  df <- readRDS(file=Filename01)
  df2 <- readRDS(file=Filename02)
  df3 <- readRDS(file=Filename03)
  df4 <- readRDS(file=Filename04)
  df<-rbind(df4,df3,df2,df)
  rm(df_2)
  rm(df_3)
  rm(df_4)
}

df2 <- str_replace_all(df$tweet_body,"https?://[0-9A-Za-z\\./\\?]+","")
df2 <- str_replace_all(df2,"@[0-9A-Za-z\\./\\?\\:]+","")
df2 <- str_replace_all(df2,"[^0-9a-zA-Z# ]", "")
df2 <- str_replace_all(df2,"RT ", "")
df2 <- save_tolower(df2)
length(df2) #Check number of rows (i.e., tweets)

df3 <- str_replace_all(df2, "ebola", "")
# We remove the word "Ebola" for the DTM and LDA;
# but for this dataset, we keep "CDC"
# because not every tweet has "CDC" per se - as "CDC" exists in "CDCchat" for example

df3 <- str_replace_all(df3, "amp", "")
# We remove ampersand

ap.corpus <- Corpus(VectorSource(df3)) # Use df3 to create the corpus for DTM
ap.corpus <- tm_map(ap.corpus, removePunctuation)
#ap.corpus <- tm_map(ap.corpus, content_transformer(tolower)) #This line generates errors due to emoji

# Create Document-Term-Matrix
topicDTM <- DocumentTermMatrix(ap.corpus,
                               control=list(wordLengths=c(1,Inf),
                                            stopwords=stopwords('en'),
                                            removePunctuation = function(x)
                                              removePunctuation(x, preserve_intra_word_dashes = T),
                                            stemming=function(x) stemDocument(x, "en")))
#topicDTM
topicDTM1<-removeSparseTerms(topicDTM, (nrow(topicDTM)-1) / nrow(topicDTM))
#topicDTM1
topicDTM2<-removeSparseTerms(topicDTM, (nrow(topicDTM)-2) / nrow(topicDTM))
#topicDTM2
topicDTM3<-removeSparseTerms(topicDTM, (nrow(topicDTM)-3) / nrow(topicDTM))
#topicDTM3

term_freq <- colSums(as.matrix(topicDTM3))

check_rowSums <- rowSums(as.matrix(topicDTM3))
#is.vector(check_rowSums) #should be "TRUE"
#check_rowSums[which.min(check_rowSums)] #check if there is any item that is zero.

if(check_rowSums[which.min(check_rowSums)] ==0){
topicDTM3a<-topicDTM3[which(rowSums(as.matrix(topicDTM3)) > 0),]
#topicDTM3a
DTM_to_use <- topicDTM3a
} else {
  DTM_to_use <- topicDTM3 #Need manual input to select the DTM to use
}
#DTM_to_use


#Filename_term_freq
write.csv(term_freq, Filename_term_freq)

# Written by Mr. Chung-Hong Chan, University of Hong Kong
# Date: June 2, 2016.
# This is how I do the cross validation. Concretely, it randomly select
# 90% of the data as the training set and use the rest of 10% as
# holdout.
# This is the code for you to determine the optimal k with cross validation.
# After you select the best k from the perplexity-k curve with elbow method,
# then fit a topic model with ALL data using that optimal k.

#require(plyr)
#replication <- 1000
dtm <- DTM_to_use
#sequ <-seq(5,100,5) [as.integer(jobID)]

args <- commandArgs(trailingOnly=TRUE)

sequ <- as.integer(args[1])
rep  <- as.integer(args[2])

cal_Ops <- function(n, dtm, ratio = 0.1) {
  print(n)
  selVect <- sample(nrow(dtm), nrow(dtm) * ratio)
  holdout <- dtm[selVect,]
  training <- dtm[-selVect,]
  topModel <- LDA(training, n, control = list(estimate.alpha = FALSE))
  return(c(n, perplexity(topModel, holdout), as.numeric(logLik(topModel))))
}


#perplex <- ldply(5, function(x, dtm) {
#   t(replicate(1, cal_Ops(x, dtm))) } , dtm = DTM_to_use)


perplex <- as.data.frame(t(cal_Ops(sequ,dtm)))

colnames(perplex) <- c("numTopic", "perplexity", "logLike")

thefile <- "/home/mperkins/scratch/test-outdir/perplex"
thefile <- paste(thefile,".csv",sep="")

#thefile <- paste(thefile,sequ,sep="-")
#thefile <- paste(thefile,sequ,".csv",sep="")

#write.csv(perplex, file=thefile, append=T)
write.table(perplex, file=thefile, row.names=T,na="NA",append=T, quote= FALSE, sep=",", col.names=F)

proc.time()
