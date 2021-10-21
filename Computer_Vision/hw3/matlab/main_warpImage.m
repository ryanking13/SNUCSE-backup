img1 = double(imread('../data/taj1r.png'))/255;
img2 = double(imread('../data/taj2r.png'))/255;

% warpImage_p1 = [
%     1041 162;
%     873 364;
%     1234 342;
%     736 448;
%     1407 423;
%     1057 508;
%     855 618;
%     1270 603;
%     709 751;
%     1066 721;
%     1462 748;
%     1069 838;
% ];
% 
% warpImage_p2 = [
%     478 145;
%     270 322;
%     646 361;
%     87 394;
%     783 454;
%     456 495;
%     223 595;
%     651 615;
%     13 739;
%     441 714;
%     799 747;
%     432 834;
% ];
% 
% save('points.mat', 'warpImage_p1', 'warpImage_p2', '-append');

load('points.mat', 'warpImage_p1', 'warpImage_p2');
p1 = warpImage_p1;
p2 = warpImage_p2;
p1 = transpose(p1);
p2 = transpose(p2);

H = computeHnorm(p1, p2);

[Iwarp, Imerge] = warpImage(img1, img2, H);

figure(1);
imshow(Iwarp);
% saveas(gcf, "Iwarp.png");
figure(2);
imshow(Imerge);
% saveas(gcf, "Imerge.png");
