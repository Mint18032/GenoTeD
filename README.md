# TESTREST

## Workflow

![](src/main/resources/img/workflow.png)

## Approach

### Generating Operation Dependency Graph

- Computes Operation Dependency Graph (ODG) - models data dependencies among operations, helps in sorting the operations to test depending on their data dependencies (E.g: oper1's output is the input for oper2 -> test oper1 first)

### Construction

- A directed graph G = (N, V):
N is the node represents an operation, v is the edge with
v = n2 → n1, when there exists a data dependency between
n2 (request, input) and n1 (response, output).
- n1 should be tested before n2.

### Dependency Inference

Comparison operators used to
match field names:

- Case insensitive: The comparison is case insensitive,
to work around developer mistakes in using a consistent
casing across operations;
- Id completion: add prefix to a field named *id*
  - If it is a field of a structured
object, the prefix is the name of the object. E.g., the
field *id* of the object pet is renamed *petId*.
  - If this field is not part of a structured object, it
is prefixed with the name of the operation in which
it is involved, after removing get/set verbs from the operation name. E.g, the operation getPet becomes Pet after removing the verb “get”, and it is
used to change the field *id* into *petId*; (???)
- Stemming: Using Porter Stemming algorithm to each parameter name to compare. E.g, pet & pets are considered the same as the root pet.

## Implementation

- Sử dụng [Swagger parser](https://github.com/swagger-api/swagger-parser)
