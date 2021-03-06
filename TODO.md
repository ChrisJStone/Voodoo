# Tweaker

use a tweakClass for forge (bundle bootstrapper in tweakClass ?)


# Scripting

- [x] Script Definition
- [ ] parse script folder and init pack with filename as `id`
- [ ] add extra scripts for defining tome / doc generators

# Reorganize buildscripts / Repo

- [x] no code in the root project
- [ ] all dependencies in `buildSrc`
- [ ] move kotlin `src` one level higher (`src/main/kotlin` is unnecessary)

# documentation generator | tome

use html-dsl in .kts files
maybe css-dsl
search for markdown-dsl or make one

copy generated files into github pages docs folder
or other targets

## diff

copy state of the pack from git history, using tags

diff on lockfilles

# condense module graph
improve buildspeed and publish speed in dev

# generate gradle setup

needs to be done outside.. maybe via kotlin-js
requires testing the gradle setup before generating it

- create framework
- add pack

- <s> update kscript annotations/header </s>

# analyse
## list optional dependencies gradle task

list all optional dependencies of curse mods
- curse mods only
- print code to copy-paste

use modalyzer on all mod jars

## suggest forge version

build first
use modalyzer output of all jars
suggest named forge versions (copy-paste ready)

# deprecate flat entries

entries are flattened fast enough from the nested format, thee is no reason for a multi step process anymore
write flat entries to disk for debugging purposes only

# curse import

- match client and server pack contents


# config tweaks

move more deployment options into the modpack configuration
examples:
 - skcraft
 - multimc
 - curse
   deployment: `id`, `name`, `description`

# multim mc integration

trigger by holding **shift**
figure out alternative ways of detecting keyboard state at startup

options:
  - change feature selection
  - force reinstall
  
  
sort out windows file locking issues

# misc

curse-server zip export

# coroutines

ensure proper use of CoroutineContext everzwhere

## Actors
- use Actors instead of synchronized mutable lists
https://github.com/Kotlin/kotlinx.coroutines/blob/master/coroutines-guide.md#actors
since the actor is in its own coroutine context it can modify its private state without locking issues
