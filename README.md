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

Please refer to [the documentation](https://elephant-track.github.io) for details.

---

### Installing the ELEPHANT Client

Please follow the instructions in [the documentation](https://elephant-track.github.io/#/?id=installation).

## Acknowledgements

- [Mastodon](https://github.com/mastodon-sc/mastodon)
- [BigDataViewer](https://github.com/bigdataviewer/bigdataviewer-core)
- [SciJava](https://scijava.org/)
- [Unirest-java](http://kong.github.io/unirest-java/)
  
## Citation

Please cite our paper on [bioRxiv](https://biorxiv.org/cgi/content/short/2021.02.26.432552v1).

- Sugawara, K., Cevrim, C. & Averof, M. [*Tracking cell lineages in 3D by incremental deep learning.*](https://biorxiv.org/cgi/content/short/2021.02.26.432552v1) bioRxiv 2021. doi:10.1101/2021.02.26.432552

```.bib
@article {Sugawara2021.02.26.432552,
	author = {Sugawara, Ko and Cevrim, Cagri and Averof, Michalis},
	title = {Tracking cell lineages in 3D by incremental deep learning},
	elocation-id = {2021.02.26.432552},
	year = {2021},
	doi = {10.1101/2021.02.26.432552},
	publisher = {Cold Spring Harbor Laboratory},
	abstract = {Deep learning is emerging as a powerful approach for bioimage analysis, but its wider use is limited by the scarcity of annotated data for training. We present ELEPHANT, an interactive platform for cell tracking in 4D that seamlessly integrates annotation, deep learning, and proofreading. ELEPHANT{\textquoteright}s user interface supports cycles of incremental learning starting from sparse annotations, yielding accurate, user-validated cell lineages with a modest investment in time and effort.Competing Interest StatementKS is employed part-time by LPIXEL Inc.},
	URL = {https://www.biorxiv.org/content/early/2021/02/26/2021.02.26.432552},
	eprint = {https://www.biorxiv.org/content/early/2021/02/26/2021.02.26.432552.full.pdf},
	journal = {bioRxiv}
}
```

<div style="text-align: right"><a href="https://www.biorxiv.org/highwire/citation/1813952/bibtext">download as .bib file</a></div>

## License

[BSD-2-Clause](LICENSE)