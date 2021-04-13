# C- Compiler Project

## Overview

This project involves developing a compiler for the C- programming language, a simplified version of C designed for educational purposes. The compiler translates C- source code into assembly language for the Tiny Machine (TM) simulator. This project was for the CIS4650 Compilers course.

## Features

- **Lexical and Syntactic Analysis:** Generates an abstract syntax tree (AST) from C- source code.
- **Semantic Analysis:** Performs type checking and outputs symbol tables.
- **Code Generation:** Produces TM assembly language code that can be executed on the TM simulator.

## Project Structure

The project is organized into checkpoints that build off of the previous one, each with its own folder and README for detailed instructions and report:

1. **Scanner:**

   - **Purpose:** Preprocesses SGML-marked documents, tokenizing relevant information and filtering out irrelevant tags.
   - **Implementation:** Uses Java and JFlex to recognize and match tags, ensuring proper nesting and structure.
   - **Usage:** See [`Scanner/README`](./Scanner/README) for information and instructions for the scanner.

2. **Checkpoint 1: Lexical and Syntactic Analysis**

- **Focus:** Implementation of the scanner and parser to generate an abstract syntax tree (AST).
  - **Details:** See [`Checkpoint1/README`](./Checkpoint1/README) for setup and execution instructions.

3. **Checkpoint 2: Semantic Analysis**

   - **Focus:** Type checking and symbol table generation.
   - **Details:** See [`Checkpoint2/README`](./Checkpoint2/README) for setup and execution instructions.

4. **Checkpoint 3: Code Generation**
   - **Focus:** Final compilation to TM assembly language.
   - **Details:** See [`Checkpoint3/README`](./Checkpoint3/README) for setup and execution instructions.

## Development Environment

- **Platform:** The project is designed to compile and run on a Linux server.
- **Dependencies:** Requires Java and JFlex for the scanner component.

## Getting Started

1. Clone the repository.
2. Navigate to each checkpoint's folder and follow the instructions in the respective README files to build and run the components.
3. Ensure you have access to a Linux environment for development and testing.
