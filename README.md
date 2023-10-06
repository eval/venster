# Venster

A [Babashka](https://babashka.org/) CLI to render the Links-section of a README.md in your terminal to make them conveniently clickable. Templating is supported to allow for contextualized links.

<img width="574" alt="Screenshot 2023-10-04 at 14 26 22" src="https://github.com/eval/venster/assets/290596/18ee6c58-529f-4202-8d51-f871a499be66">

## Rationale

I started experimenting with having a Links-section in READMEs to quickly jump to places while developing: e.g. the project on GitHub, the issue list, the notifications, anything assigned to me but also any link to staging/production front- and backends.  
A quick experiment with an awk-script, [glow](https://github.com/charmbracelet/glow) as a pager and iTerm, which makes urls easily clickable, showed it was indeed something useful that removed friction.  
The current version allows for placeholders, i.e. making links more contextualized as they can contain the current branch, env-vars and one's git(hub|lab) handle which makes it more suitable to use with a team.


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

1. Create a section '## Links' (any depth is ok) in the project's `README.md`
1. Add links
   Available values for [Selmer filters](https://github.com/yogthos/Selmer?tab=readme-ov-file#built-in-filters-1):  
   - `env`  
     e.g. `file:/{{ env.HOME }}/.vimrc`. Which is a link that your terminal can let you open with an editor.
   - `current-branch`  
     Current git branch, e.g. `https://github.com/eval/venster/actions?query=branch%3A{{ current-branch | default:"main" }}`.
   - `github-user`  
     e.g. `https://github.com/eval/venster/issues/assigned/{{ github-user }}`, or
     to have a fallback when the value is not present: `https://github.com/eval/venster/issues{% if github-user %}/assigned/{{ github-user }}{% endif %}`.  
     Determined via...  
     - env-var `VENSTER_GITHUB_USER`.
     - git config github.user  
       Add via `git config --global github.user eval`
   - `gitlab-user`  
     Determined via...  
     - env-var `VENSTER_GITLAB_USER`.
     - git config gitlab.user  
       Add via `git config --global gitlab.user eval`
   - `readme-folder`  
     Folder where `README.md` is located. Allows for pointing to project-files,  
     e.g. `{{ readme-folder }}/.github/workflows/ci.yml`.  
1. Invoking `venster` in any subfolder of the project will now render the links.

### Example

Example Links-section in a `README.md` (see also the Links-section of this README):
```
## Links

- [Repository](https://github.com/eval/venster)

### Pull Requests

<!-- This comment won't be rendered.
     Pull Requests across multiple repositories assigned to user with fallback in case there's no github-user -->
- [My PRs](https://github.com/pulls?q=is%3Apr+archived%3Afalse+repo%3Aeval%2Fmalli-select+repo%3Aeval%2Fdeps-try+is%3Aopen{% if github-user %}+assignee%3A{{ github-user }}+{% endif %})

### Builds

- [Builds current branch](https://github.com/eval/venster/actions?query=branch%3A{{ current-branch | default:"main" }})
- [Release workflow     ]({{ readme-folder }}/.github/workflows/release.yml)

### API Docs

- [Ruby docs](https://docs.ruby-lang.org/en/{{ env.RUBY_VERSION|drop-last:2|join }})
```

### Dev

``` shell
# from $PROJECT_ROOT
$ bb -m eval.venster some args

# bbin install
$ bbin install . --as venster-dev

# ...then in another project
$ venster-dev
```

## Wishlist

- ...

## Links

- [Project               ](https://github.com/eval/venster)
- [Current branch        ](https://github.com/eval/venster/tree/{{ current-branch | default:"main" }})
- [My Issues             ](https://github.com/eval/venster/issues{% if github-user %}/assigned/{{ github-user }}{% endif %})
- [My PRs                ](https://github.com/eval/venster/pulls{% if github-user %}/assigned/{{ github-user }}{% endif %})

### Builds

- [Builds current branch](https://github.com/eval/venster/actions?query=branch%3A{{ current-branch | default:"main" }})
- [Release workflow     ]({{ readme-folder }}/.github/workflows/release.yml)


### Releasing

- [Stable](https://github.com/eval/venster/releases/tag/stable)
- [Unstable](https://github.com/eval/venster/releases/tag/unstable)
- [Homebrew repository   ](https://github.com/eval/homebrew-brew)

> Clojure solves the problem that you don't know you have. -- Rich Hickey

## License

Copyright (c) 2023 Gert Goet, ThinkCreate. Distributed under the MIT license. See [LICENSE](./LICENSE).

