import java.util.Queue;
import java.util.LinkedList;
import java.util.Collections;
Queue<String> voiceQueue = new LinkedList<String>();

void addVoices(){
  
  ArrayList<String> voices = new ArrayList<String>();
  String[] strings = loadStrings("Negatives.txt");
  voiceQueue = new LinkedList<String>();
  
  for(int i = 1; i < strings.length; i++){
    voices.add(strings[i]);
  }
  
  Collections.shuffle(voices);
  
  for(String s : voices){
    voiceQueue.add(s);
  }
}

void addFinalVoices(){
}