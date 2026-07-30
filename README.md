==Update 2026.07.29==

The DiffractOgram applet is now fully functional using Jmol (DiffractOgram2; Java and JavaScript) or Java3D (DiffractOgram; Java only) for rendering the Java3D "world" environment. 

DiffractOgram:
<img width="1369" height="624" alt="image" src="https://github.com/user-attachments/assets/e38deb25-8138-4104-b51e-94a859c7cb00" />

DiffractOgram2:
<img width="1407" height="546" alt="image" src="https://github.com/user-attachments/assets/5cd6334c-c4ea-4ec2-8980-e02d9f34615f" />

Both applications have a few differences from the original. In both cases the origin of the refracted beam -- originally from the center of the offset reciprocal lattice -- has been moved to the center of this sphere. This seemed more intuitive, since the refraction is occurring from the crystal, not the reciprocal lattice origin. It also makes it clearer that the reciprocal lattice is acting as a "filter", allowing to pass only reflections that fit the constraints of integral h k l. The specific reciprocal atom points that meet this criterion (by touching the Ewald sphere) are highlighted in red. In addition, several minor bugs or GUI issues were fixed.   

In DiffractOgram2, the text banners that rotate with the screen, originally viewable forwards and backwards, upside right and upside down, depending upon the viewing angle, are gone. These could have been implemented in Jmol in principle, but I decided not to do that, mostly because I found them distracting when viewing from different angles. In Diffractogram2, during animations, the reciprocal lattice is recreated point by point rather than being displayed fully when the animation starts. The result is a dynamic generation of the selected range of the reciprocal lattice using actual reflections and, I think, a more interesting visualization.

In DiffractOgram2 the goniometer is not shown. Instead, normalized crystal a, b, and c axes are indicated on a wave-length-independent unit sphere. Making the this Ewald sphere a unitless unit sphere allows us to visualize the allowed reflections as points along a unitless number line that is laid out along each of the three crystal axes, where the indexes are h, k, and l:

<img width="570"  alt="image" src="https://github.com/user-attachments/assets/d1ffb11a-caa5-4f3a-86c4-6181aaeea7cf" />

It also suggests visualizing these as dot products of the unit beam vectors S and So with the individual crystal axes:

<img width="1066" height="652" alt="image" src="https://github.com/user-attachments/assets/b1c3cf4d-064a-47b5-a7e1-42566eac1834" />

where the numbers are integer multiples of lambda/axis length. So in this visualization, when the wavelength is changed, the size of the unit sphere is unchanged. Rather, the size of the "scaled reciprocal lattice" changes. (Its lattice size is proportional to lambda.) This allows one to observe that the components of normalized vector difference k(out) - k(in) (or S - So) along the crystal axis must be integral multiples of lambda/a, lambda/b, and lambda/c, respectively. (This is the black line in the visualization, where So is gold, and S is red.) This is the triple constraint that produces constructive interference and the observation of refraction in the projection -- exactly as for the simple Bragg equation for refraction in a plane. 

<img width="741" height="631" alt="image" src="https://github.com/user-attachments/assets/0a13a04b-1f33-42eb-80fd-344b347deaa2" />
 
What's nice about this (but not visualized in the app) is that with a bit of algebra, this becomes

<img width="337" height="436" alt="image" src="https://github.com/user-attachments/assets/9289c9fe-6b27-4f4f-9e5f-72ad7be9e3d6" />

and is the essence of what is referred to as Miller planes:

<img width="1137" height="502" alt="image" src="https://github.com/user-attachments/assets/23646b74-974c-4a8d-bc8b-ad100ff0ed6c" />



==Initial work==
Current status: working in JavaScript
- no text or "torus"
- a bit slow in performance


See BH_NOTES.txt


An experiment in getting diffractOgram 
working in JavaScript. 

The main issue is that diffractOgram uses Java3D for its graphics.

The idea is to dissociate the model from the rendering (done) 
and then see what we can do for a simplistic replacement that may or may not use WebGL


