# Venster

CLI written in [Babashka](https://babashka.org/) to render the Links-section of a README.md in your terminal. Templating is supported to allow for contextualized links.

<img width="574" alt="Screenshot 2023-10-04 at 14 26 22" src="https://github.com/eval/venster/assets/290596/18ee6c58-529f-4202-8d51-f871a499be66">

## Rationale

I started experimenting with having a Links-section in READMEs to quickly jump to places while developing: e.g. the project on GitHub, the issue list, the notifications, anything assigned to me but also any link to staging/production front- and backends.  
A quick experiment with an awk-script, [glow](https://github.com/sharkdp/glow) as a pager and iTerm, which makes urls easily clickable, showed it was indeed something useful that removed friction.  
The current version allows for placeholders, i.e. making links more contextualized as they can contain the current branch and one's git(hub|lab) handle which makes it more suitable to use with a team.


## Install

### homebrew

``` shell
$ brew install eval/brew/venster
```

### bbin

#### Prerequisites

* babashka
* bbin
* glow

#### Install

``` shell
$ bbin install https://github.com/eval/venster/releases/download/stable/venster-bb.jar --as venster
```

## Usage

### Dev

``` shell
# from $PROJECT_ROOT
$ bb -m eval.venster some args

# bbin install
$ bbin install . --as venster-dev

# ...then in another project
$ venster-dev
```

## NextUp

- 

## Links

- [Project               ](https://github.com/eval/venster)
- [Current branch        ](https://github.com/eval/venster/tree/{{branch|default:"main"}})
- [My Issues             ](https://github.com/eval/venster/issues{% if gh-handle %}/assigned/{{gh-handle}}{% endif %})
- [Homebrew repository   ](https://github.com/eval/homebrew-brew)


### Releases

- [Stable](https://github.com/eval/venster/releases/tag/stable)
- [Unstable](https://github.com/eval/venster/releases/tag/unstable)

> Clojure solves the problem that you don't know you have. -- Rich Hickey

## License

Copyright (c) 2023 Gert Goet, ThinkCreate. Distributed under the MIT license. See [LICENSE](./LICENSE).
