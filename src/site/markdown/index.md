# Markdown Syntax #

The [Maven doxia module] for [Markdown] uses the [pegdown] processor
to render HTML. Pegdown supports several extensions to the Markdown
syntax. This documents shows example of syntax you can use in your
Maven site and the results.

## Text ##

| Markup                              | Result                          |
|:-----------------------------------:|:-------------------------------:|
| ``This text is **strong**.``        | This text is **strong**.        |
| ``This text is also __strong__.``   | This text is also __strong__.   |
| ``This text is *emphasized*.``      | This text is *emphasized*       |
| ``This text is also _emphasized_.`` | This text is also _emphasized_. |
| ```This is ``some code``.```        | This is ``some code``.          |
| ``This is also ```some code```.``   | This is also ```some code```.   |

## Headings ##

See [this page](headings.html).

## Lists ##

## Links ##

## Images ##

## Code ##

## Footnotes ##

Here is some text containing a footnote[^somesamplefootnote]. You can then continue your thought...

[^somesamplefootnote]: Here is the text of the footnote itself.

Even go to a new paragraph and the footnotes with go to the bottom of the document[^documentdetails].

[^documentdetails]: Depending on the final form of your document, of course. See the documentation and experiment.

    This footnote has a second paragraph.

## Tables ##

### Simple Table ###

#### Markup ####

```
| First Header | Second Header |         Third Header |
| :----------- | :-----------: | -------------------: |
| First row    |      Data     | Very long data entry |
| Second row   |    **Cell**   |               *Cell* |
```

#### Result ####

| First Header | Second Header |         Third Header |
| :----------- | :-----------: | -------------------: |
| First row    |      Data     | Very long data entry |
| Second row   |    **Cell**   |               *Cell* |

### Table with cells spanning multiple columns ###

#### Markup ####

```
|              | Grouping                    ||
| First Header | Second Header | Third Header |
| ------------ | :-----------: | -----------: |
| Content      | *Long Cell*                 ||
| Content      | **Cell**      | Cell         |
| New section  | More          | Data         |
```

#### Result ####

|              | Grouping                    ||
| First Header | Second Header | Third Header |
| ------------ | :-----------: | -----------: |
| Content      | *Long Cell*                 ||
| Content      | **Cell**      | Cell         |
| New section  | More          | Data         |


[Maven doxia module]: http://maven.apache.org/doxia/doxia/doxia-modules/doxia-module-markdown/
[Markdown]: http://daringfireball.net/projects/markdown/ "Main Markdown site"
[pegdown]: https://github.com/sirthias/pegdown


