circleThreshold = 90/255; %set this value
darkThreshold   = 100/255; %set this value

%path to sphere and object images
datapath = '../data/q2';

%read in the ambient light image
img1 = double(imread(sprintf('%s/sphere0.pgm', datapath)))/255;

%compute radius and center of sphere
[cx, cy, r] = findCircle(img1, circleThreshold);

%find directions of light sources
img1 = double(imread(sprintf('%s/sphere1.pgm', datapath)))/255;
img2 = double(imread(sprintf('%s/sphere2.pgm', datapath)))/255;
img3 = double(imread(sprintf('%s/sphere3.pgm', datapath)))/255;

lv1 = findLight(img1, cx, cy, r);
lv2 = findLight(img2, cx, cy, r);
lv3 = findLight(img3, cx, cy, r);

%read in images of object under different lighting conditions
img1 = double(imread(sprintf('%s/object1.pgm', datapath)))/255;
img2 = double(imread(sprintf('%s/object2.pgm', datapath)))/255;
img3 = double(imread(sprintf('%s/object3.pgm', datapath)))/255;

%compute normals and albedos with photometric stereo
[normals, albedo] = computeNormals( img1, img2, img3, lv1, lv2, lv3, darkThreshold);

%Visualization only below this line
 step = 10;
 X = 1:step:size(img1,2);
 Y = 1:step:size(img1,1);
 U = reshape(normals(1,:), size(img1));
 V = reshape(normals(2,:), size(img1));
 U = U(1:10:end, 1:10:end);
 V = V(1:10:end, 1:10:end);
 
 figure(1);
 hold off;
 imshow(img1);
 hold on;
 quiver(X,Y,U,V);
 title('Computed Surface Normals');
%  saveas(gcf, 'q2normal.png');
 
 figure(2);
 imagesc(albedo);
 title('Unnormalized Albedo');
%  saveas(gcf, 'q2albedo.png');
