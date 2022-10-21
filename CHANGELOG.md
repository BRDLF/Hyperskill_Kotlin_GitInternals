# Hyperskill_Kotlin_GitInternals

### Stage 6/7: Git Log

added `log` command.

`log` shows the commit history, providing committer, commit timestamp, and commit message.

### Stage 5/7: Branches

Program now asks for directory, then for a command.
The valid commands are `list-branches` & `cat-file`


`list-branches` shows a list of the branches, indicating the current branch with a `*`

`cat-file` behaves as the program did before, prompting for a hash and then displaying the contents of the associated git object.

### Stage 4/7: Trees

Program now supports trees.

### Stage 3/7: Commits

Program asks for the .git directory and a hash value.
Then outputs contents, if Commit, output is formatted slightly.

### Stage 2/7: Git object types

Program now asks for the .git directory and a hash value.
Then outputs header information `type:<type> length:<length>`

### Stage 1/7: What is a Git object

Not much to say here. Haven't had to read files in a while. Hopefully this teaches something valuable.