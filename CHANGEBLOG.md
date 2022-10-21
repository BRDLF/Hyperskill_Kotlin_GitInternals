# Hyperskill_Kotlin_GitInternals

### Stage 7/7: Full tree

It's done now. I know I usually go on these big long asides about how it was, but it was miserable.
I kept things sloppy in order to save time and rush the work, but I think the sloppiness may have cost me time overall, it's hard to say.
There's not very good error handling, file separators are interpreted by OS in most cases but hardcoded as `/` for the `commit-tree` command due to testing requirements.

It's a big mess, but it's done! I've finished the project (and also now, the course.) \
Now, onto other things, hopefully ones that feel more meaningful.

### Stage 6/7: Git Log

This is gross. I mean, it works but feels like I built it with paperclips. That's ok. I'm almost done.

### Stage 5/7: Branches

Finally did the needful and put things into an object. Definitely helps tidy things. \
I could have chosen to make things static but decided that really isn't necessary for the scope of the tool. It mostly just adds global scope and complicates my job. \
Besides reorganizing code, implementing the branch list wasn't terribly difficult. Neither was prompting separately for an action.
A little upset, because i'm sure there's some perfect, elegant way for me to write this code, but I don't need it to be elegant I need it to work. I'm learning to be satisfied with that. (for now)

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