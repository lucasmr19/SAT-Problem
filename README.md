# SAT-Problem in Java

This project is an implementation of the **Boolean Satisfiability Problem** (SAT) in Java. The algorithm's used to solve the problem are the Local Search of Solutions. Here is a detailed description of the main classes used for this project.

## Table of Contents

1. [Overview](#overview)
2. [Main Classes](#main-classes)
    - [CNF.java](#cnf-java)
    - [LocalSearchSolution.java](#localsearchsolution-java)
    - [RandomConfigGenerator.java](#randomconfiggenerator-java)
    - [SAT.java](#sat-java)
    - [ConvertToDec.java](#converttodec-java)

## Overview

The **Boolean Satisfiability Problem** (SAT) is a fundamental problem in computer science and artificial intelligence. Given a set of clauses of disjunctions (a boolean formula in conjunctive normal form), the problem is to determine whether there exists a truth assignment to the variables that makes the formula true.

The project provides an implementation of local search of solutions to solve the SAT problem. The basic idea is to find a solution through iterations and incremental changes.

## Main Classes

### CNF.java

This class represents a boolean formula in Conjunctive Normal Form (CNF). A CNF consists of a series of clauses, where each clause is a disjunction of literals. The `CNF` class provides methods for evaluating a variable configuration in the CNF formula and for counting the clauses that are not true for a given configuration.

### LocalSearchSolution.java

This class represents a solution found by the local search algorithm. It stores a variable configuration, as well as the number of tries and flips performed to find this solution.

### RandomConfigGenerator.java

This class is used to generate random variable configurations. It provides a `getRandomConfig` method that generates an array of size `n` with random values of `-1` and `1`, which represent truth assignments to variables.

### SAT.java

This class contains the main `solveSAT` method, which solves the SAT problem using local search of solutions. It takes a CNF formula, the maximum number of tries, and the maximum number of flips as input and returns a local solution.

### ConvertToDec.java

This class provides a static `convertToDec` method to convert a variable configuration represented as an array of `1` and `-1` into a decimal number. It is used to generate a unique index for each random configuration, which allows tracking which configurations have already been tried in the search process.

I hope this provides a clear overview of the structure and functionality of each class in the SAT-Problem project in Java.
