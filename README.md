==Update 2026.07.29==

The diffractOgram applet is now nearly fully functional in JavaScript using a JSmol interface for viewing within a Java3D "world" environment. 

It has a few differences from its original. Superficially, the text banners that rotate with the screen, originally viewable forwards and backwards, upside right and upside down, depending upon the viewing angle, are gone. This could have been implemented in Jmol in principle, but I decided not to do that. In addition, during animations, the reciprocal lattice is recreated,point by point.  

More significantly, the goniometer is not shown. Instead, normalized a, b, and c axes are indicated on a wave-length-independent unit sphere. The origin of the refracted beam, originally from the center of the offset reciprocal lattice, has been moved to the center of this sphere.  

<img width="919" height="663" alt="image" src="https://github.com/user-attachments/assets/7f35e6f1-7dc8-4fc8-b168-5da4be0c015d" />


Making the Ewald sphere a unitless unit sphere allows us to visualize the allowed refractions as points along a unitless numberline that is laid out along each of the three crystal axes, where the indexes are h, k, and l:

<img width="570"  alt="image" src="https://github.com/user-attachments/assets/d1ffb11a-caa5-4f3a-86c4-6181aaeea7cf" />

It also suggests grouping these as dot products of the unit beam vectors S and So with the individual crystal axes:

<img width="1066" height="652" alt="image" src="https://github.com/user-attachments/assets/b1c3cf4d-064a-47b5-a7e1-42566eac1834" />


So in this visualization, when the wavelength is changed, the size of the unit sphere is unchanged. Rather, the size of the "scaled reciprocal lattice" changes. (Its size is proportional to lambda.) This allows one to observe that the components of normalized vectors k(out) - k(in) must be integral multiples of lambda/a, lambda/b, and lambda/c, respectively, in order for constructive interference and the observation of refraction -- exactly as for the simple Bragg equation for refraction in a plane. 

<img width="741" height="631" alt="image" src="https://github.com/user-attachments/assets/0a13a04b-1f33-42eb-80fd-344b347deaa2" />
 

What's nice about this is that with a bit of algebra, this becomes

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


