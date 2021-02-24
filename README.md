## ELEPHANT: Tracking cell lineages in 3D by incremental deep learning

<table>
  <tbody>
    <tr>
      <th rowspan=7><img src="../assets/incremental-training-demo.gif?raw=true"></img></th>
    </tr>
    <tr>
      <th colspan=2><img src="../assets/elephant-logo-text.svg" height="64px"></th>
    </tr>
    <tr>
      <td>Developer</td>
      <td><a href="http://www.ens-lyon.fr/lecole/nous-connaitre/annuaire/ko-sugawara">Ko Sugawara</a></td>
    </tr>
    <tr>
      <td valign="top">Forum</td>
      <td><a href="https://forum.image.sc/tag/elephant">Image.sc forum</a><br>Please post feedback and questions to the forum.<br>It is important to add the tag <code>elephant</code> to your posts so that we can reach you quickly.</td>
    </tr>
    <tr>
      <td>Source code</td>
      <td><a href="https://github.com/elephant-track">GitHub</a></td>
    </tr>
    <tr>
      <td>Publication</td>
      <td><a href="https://www.biorxiv.org/content">bioRxiv</a></td>
    </tr>
  </tbody>
</table>


---
ELEPHANT is a platform for 3D cell tracking, based on incremental and interactive deep learning.

It works on client-server architecture. The server is built as a web application that serves deep learning-based algorithms.

This repository provides an implementation of the ELEPHANT client, which is implemented by extending [Mastodon](https://github.com/mastodon-sc/mastodon), providing a user interface for annotation, proofreading and visualization.
The ELEPHANT server can be found [here](https://github.com/elephant-track/elephant-server).

Please refer to [the documentation]() for details.

---

### ELEPHANT Client Requirements

|                  | Requirements                                                                                                              |
| ---------------- | ------------------------------------------------------------------------------------------------------------------------- |
| Operating System | Linux, Mac or Windows OS                                                                                                  |
| Java             | Java Runtime Environment 8 or higher                                                                                      |
| Storage          | Sufficient size for your data (Please consider using [BigDataServer](https://imagej.net/BigDataServer) for the huge data) |


### Installing the ELEPHANT Client

The ELEPHANT client works as a plugin for [Mastodon](https://github.com/mastodon-sc/mastodon).
However, because ELEPHANT was built on a specific version of Mastodon, with minor customization,
we ask users to download and use a self-contained executable instead of the official release available on [Fiji](https://imagej.net/Fiji).

| Info <br> :information_source: | Mastodon user manual is available [here](https://github.com/mastodon-sc/mastodon-manual/blob/pdf/MastodonManual.pdf). |
| :----------------------------: | :-------------------------------------------------------------------------------------------------------------------- |

#### 1. Download an executable jar

Please [download a zip file](https://github.com/elephant-track/elephant-client/releases/download/v0.1.0/elephant-0.1.0-client.zip) that contains the latest version of executable jar with dependencies in the `lib/` directory.

#### 2. Launch an application

Double click the executable jar with the name `elephant-0.1.0-client.jar`.\
Alternatively, launch an application from CLI, which is better for debugging in case of problems.

```bash
java -jar elephant-0.1.0-client.jar
```

| Info <br> :information_source: | ELEPHANT is built with `openjdk version "1.8.0_275"`. It should work with Java Runtime Environment (JRE) version 8 or higher. <br> You can download a prebuilt OpdnJDK binary [here](https://adoptopenjdk.net/). |
| :----------------------------: | :--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |

A main window will be shown as below.

<img src="../assets/main-window.png"></img>

## Development

Currently, a customized version of [Mastodon](https://github.com/mastodon-sc/mastodon), which is one of the dependencies of this project, is not managed on the public maven repository.

To build this project, please clone [this GitHub repository](https://github.com/elephant-track/mastodon/tree/trackmate-1.0.0-beta-13-elephant) and run `mvn install` after switching to the tag `trackmate-1.0.0-beta-13-elephant` on your system.

```bash
git clone git@github.com:elephant-track/mastodon.git
cd mastodon
git checkout tags/trackmate-1.0.0-beta-13-elephant
mvn install
```

## Acknowledgements

- [Mastodon](https://github.com/mastodon-sc/mastodon)
- [BigDataViewer](https://github.com/bigdataviewer/bigdataviewer-core)
- [SciJava](https://scijava.org/)
- [Unirest-java](http://kong.github.io/unirest-java/)
  
## Citation

Please cite our paper.

## License

[BSD-2-Clause](LICENSE)