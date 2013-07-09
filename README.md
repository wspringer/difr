# One-minute Introduction

If you want to do a code review, and you consider all of the existing tools too bloated, too expensive or too limited, then `difr` might be perfectly suited for you. If you pass `difr` a diff file (wherever it might come from), then `difr` will turn that diff file into an HTML file (not unlike GitHub's view of a diff) with an embedded editor for adding comments. 

Once you added all your comments, you simply save the file, or copy the entire page in your email. 

### Installation

Download a jar [here](https://www.dropbox.com/s/sbqgcseosbqwdzc/difr.jar). Other than that, all you need is Java.

### Sample output

![Sample output](http://nxt.flotsam.nl/difr.png)

### Typical use cases

To create a review page for the diff between your working copy and what's currently on HEAD:

    git diff | java -jar difr.jar > /tmp/review.html

To create a review page for your feature branch, if you're using git flow:

    git flow feature diff {feature} | java -jar difr.jar > /tmp/review.html
    
Or if that's too much trouble, pass the HTML produced directly to your browser, using [bcat](http://rtomayko.github.io/bcat/), which is what I do all the time:

    git flow feature diff {feature} | java -jar difr.jar | bcat

To get help on the options `difr` accepts, type:

    java -jar difr.jar -h



