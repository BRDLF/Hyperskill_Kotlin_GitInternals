# Hyperskill_Kotlin_GitInternals

### Stage 4/7: Trees

It seems tradition at this point to make a big rework halfway through the project. 
I was having problems with using scanner to read the 20 Byte SHA at the end of filenames. 
I decided that I should instead learn to use the input stream I already had. After some fiddling about, I was able to get the old code working without a scanner, and write something to format trees how the project asked.
It certainly feels a bit iffy, but it works. I'm just trying to get things done atm.

### Stage 3/7: Commits

I ran into an issue with the EduTools plugin not downloading files needed for testing the program.
Otherwise, this isn't terribly challenging. This code is messy, but gets the job done.

### Stage 2/7: Git object types

So far just slightly modifying the first stage. Nothing spectacular to note.

### Stage 1/7: What is a Git object

Initial program asks for a git object location, then inflates and prints the result to output.