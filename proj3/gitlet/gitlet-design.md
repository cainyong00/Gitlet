# Gitlet Design Document
author: Heeyong Chung

## Design Document Guidelines

Please use the following format for your Gitlet design document. Your design
document should be written in markdown, a language that allows you to nicely 
format and style a text file. Organize your design document in a way that 
will make it easy for you or a course-staff member to read.  

## 1. Classes and Data Structures

* Blob
   * String contents: contents of the file 
   * String name: name of the file 
   * String hash: the hash string
* Commit
  * Hashmap(?) of the contents
* Main GitLet class
  * Multiple arraylists
    * hash values
    * branches
  * Hashmap for names
  


## 2. Algorithms

* Blob
  * the _get_ functions for the instance variables of the class
* Commit
  * the _get_ functions for the instance variables of the class
  * a function that generates the hash value
* Gitlet
  * init
    * initialize a GitLet directory
  * add
    * adds a file to the staging area
  * commit
    * create and add a new commit object(?) from the passed variables in the staging area
and add this to the data structure
  * remove
    * remove a file from the staging area and also the directory itself(?)
  * rm
  * log
    * display the history(ancestry) of the current commit
  * find
    * find commit given a message
  * status
    * print out the entire status(names, previous changes)
  * checkout
    * maybe multiple checkout functions with the following commands:
      1. Takes the version of the file as it exists in the head commit, 
      the front of the current branch, and puts it in the working directory, 
      overwriting the version of the file that's already there if there is one. 
      The new version of the file is not staged.
      2. Takes the version of the file as it exists in the commit with the 
      given id, and puts it in the working directory, overwriting the 
      version of the file that's already there if there is one. The new version 
      of the file is not staged.
      3. Takes all files in the commit at the head of the given branch, 
      and puts them in the working directory, overwriting the versions of 
      the files that are already there if they exist. Also, at the end of 
      this command, the given branch will now be considered the current 
      branch (HEAD). Any files that are tracked in the current branch but are not present in the checked-out branch are deleted. The staging area is cleared, unless the checked-out branch is the current branch
    
  * branch
    * create a new branch with the passed in name, and point it at the current head node
    * nothing more than a name for reference
  * reset
    * check out all the files tracked by a given commit, remove tracked files
    that are not in the commit
  * merge
    * combine two branches into a single commit. Given a branch, merge it to the current branch

## 3. Persistence

By storing all the files by their hash value in the .gitlet folder, 
which makes them all accessible as the hash values are immutable.

initilizing, adding, commiting, removing, and merging will require the state to be 
mutated and saved.

## 4. Design Diagram

Attach a picture of your design diagram illustrating the structure of your
classes and data structures. The design diagram should make it easy to 
visualize the structure and workflow of your program.

