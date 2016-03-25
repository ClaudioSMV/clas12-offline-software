/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author gavalian
 */
public class CoatUtilsFile {
    private static String moduleString = "[CoatUtilsFile] --> ";
            
    public static String getName(){ return moduleString; }
    /**
     * prints a log message with the module name included.
     * @param log 
     */
    public static void printLog(String log){
        System.out.println(CoatUtilsFile.getName() + " " + log);
    }
    /**
     * returns package resource directory with given enviromental variable
     * and relative path.
     * @param env
     * @param rpath
     * @return 
     */
    public static String getResourceDir(String env, String rpath){
        
        String envString = System.getenv(env);
        if(envString==null){
            CoatUtilsFile.printLog("Environment variable ["+env+"] is not defined");
            envString = System.getProperty(env);
        }
        
        if(envString == null){
            CoatUtilsFile.printLog("System property ["+env+"] is not defined");
            return null;
        }
        
        StringBuilder str = new StringBuilder();
        int index = envString.length()-1;
        str.append(envString);
        //Char fileSeparator =
        if(envString.charAt(index)!='/' && rpath.startsWith("/")==false) str.append('/');
        str.append(rpath);        
        return str.toString();
    }
    /**
     * returns list of files in the directory. absolute path is given.
     * This function will not exclude ".*" and "*~" files.
     * @param directory
     * @return 
     */
    public static List<String>  getFileList(String directory){        
        List<String> fileList = new ArrayList<String>();
        File[] files = new File(directory).listFiles();
        System.out.println("FILE LIST LENGTH = " + files.length);
        for (File file : files) {
            if (file.isFile()) {
                if(file.getName().startsWith(".")==true||
                        file.getName().endsWith("~")){
                    System.out.println("[FileUtils] ----> skipping file : " + file.getName());
                } else {
                    fileList.add(file.getAbsolutePath());
                }
            }
        }
        return fileList;
    }
    /**
     * returns list of files in the directory defined by environment variable
     * and a relative path.
     * @param env
     * @param rpath
     * @return 
     */
    public static List<String>  getFileList(String env, String rpath){
        String directory = CoatUtilsFile.getResourceDir(env, rpath);
        if(directory==null){
            CoatUtilsFile.printLog("(error) directory does not exist : " + directory);
            return new ArrayList<String>();
        }
        return CoatUtilsFile.getFileList(directory);
    }
    /**
     * returns a file list that contains files with given extension
     * @param env
     * @param rpath
     * @param ext
     * @return 
     */
    public static List<String>  getFileList(String env, String rpath, String ext){
        String directory = CoatUtilsFile.getResourceDir(env, rpath);
        if(directory!=null) return new ArrayList<String>();
        
        List<String> files = CoatUtilsFile.getFileList(directory);
        List<String> selected = new ArrayList<String>();
        for(String item : files){
            if(item.endsWith(ext)==true) selected.add(item);
        }
        return selected;
    }
    /**
     * Reads a text file into a list of strings  
     * @param filename
     * @return 
     */
    public static List<String>   readFile(String filename){
        List<String>  lines = new ArrayList<String>();
        String line = null;
        try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader =  new FileReader(filename);
            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader =  new BufferedReader(fileReader);

            while((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
            }   
            // Always close files.
            bufferedReader.close();         
        }
        catch(FileNotFoundException ex) {
            CoatUtilsFile.printLog("Unable to open file : '" + filename + "'");             
        }
        catch(IOException ex) {
            CoatUtilsFile.printLog( "Error reading file : '" + filename + "'");                  
            // Or we could just do this: 
            // ex.printStackTrace();
        }
        return lines;
    }
    /**
     * Reads a text file into one string.
     * @param filename
     * @return 
     */
    public static String readFileString(String filename){
        List<String> lines = CoatUtilsFile.readFile(filename);
        StringBuilder str = new StringBuilder();
        for(String line : lines) str.append(line);
        return str.toString();
    }
    /**
     * Returs relative paths of file names from list of absolute paths.
     * @param files
     * @return 
     */
    public static List<String>  getFileNamesRelative(List<String> files){
        List<String>  newList = new ArrayList<String>();
        for(String file : files){
            int index = file.lastIndexOf('/');
            if(index>=0&&index<file.length()){
                newList.add(file.substring(index+1, file.length()));
            } else {
                newList.add(file);
            }
        }
        return newList;
    }
}
