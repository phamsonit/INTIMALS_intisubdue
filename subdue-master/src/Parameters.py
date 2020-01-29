# Parameters.py
#
# Written by Larry Holder (holder@wsu.edu).
#
# Copyright (c) 2017-2018. Washington State University.

import os

class Parameters:

    def __init__(self):
        # User-defined parameters
        self.inputFileName = ""       # Store name of input file
        self.outputFileName = ""      # Same as inputFileName, but with .json removed from end if present
        self.beamWidth = 4            # Number of patterns to retain after each expansion of previous patterns; based on value.
        self.iterations = 1           # Iterations of Subdue's discovery process. If more than 1, Subdue compresses graph with best pattern before next run. If 0, then run until no more compression (i.e., set to |E|).
        self.limit = 0                # Number of patterns considered; default (0) is |E|/2.
        self.maxSize = 0              # Maximum size (#edges) of a pattern; default (0) is |E|/2.
        self.minSize = 1              # Minimum size (#edges) of a pattern; default is 1.
        self.numBest = 3              # Number of best patterns to report at end; default is 3.
        self.prune = False            # Remove any patterns that are worse than their parent.
        self.valueBased = False       # Retain all patterns with the top beam best values.
        self.writeCompressed = False  # Write compressed graph after iteration i to file outputFileName-compressed-i.json
        self.writePattern = False     # Write best pattern at iteration i to file outputFileName-pattern-i.json
        self.writeInstances = False   # Write instances of best pattern at iteration i as one graph to file outputFileName-instances-i.json
        self.temporal = False         # Discover static (False) or temporal (True) patterns
        self.outputDot = False        # store output dot files
    
    def set_parameters (self, args):
        """Set parameters according to given command-line args list."""
        self.inputFileName = args[-1]
        filename, file_extension = os.path.splitext(self.inputFileName)
        if (file_extension == '.json'):
            self.outputFileName = filename
        else:
            self.outputFileName = self.inputFileName

        index = 1
        numArgs = len(args)
        while index < (numArgs - 1):
            optionName = args[index]
            if optionName == "--beam":
                index += 1
                self.beamWidth = int(args[index])
            if optionName == "--iterations":
                index += 1
                self.iterations = int(args[index])
            if optionName == "--limit":
                index += 1
                self.limit = int(args[index])
            if optionName == "--maxsize":
                index += 1
                self.maxSize = int(args[index])
            if optionName == "--minsize":
                index += 1
                self.minSize = int(args[index])
            if optionName == "--numbest":
                index += 1
                self.numBest = int(args[index])
            if optionName == "--prune":
                self.prune = True
            if optionName == "--valuebased":
                self.valueBased = True
            if optionName == "--writecompressed":
                self.writeCompressed = True
            if optionName == "--writepattern":
                self.writePattern = True
            if optionName == "--writeinstances":
                self.writeInstances = True
            if optionName == "--temporal":
                self.temporal = True
            if optionName == "--outputDotFile":
                self.outputDot = True
            index += 1
