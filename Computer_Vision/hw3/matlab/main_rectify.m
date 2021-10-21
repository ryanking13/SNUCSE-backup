img = double(imread('../data/HTC.png'))/255;

% rectify_p1 = [76 43;
%     230 50;
%     110 367;
%     264 357;];
% 
% rectify_p2 = [107 50;
%     270 50;
%     107 355;
%     270 355;];
% save('points.mat', 'rectify_p1', 'rectify_p2', '-append');

load('points.mat', 'rectify_p1', 'rectify_p2');
p1 = rectify_p1;
p2 = rectify_p2;

p1 = transpose(p1);
p2 = transpose(p2);

[H, result] = rectify(img, p1, p2);

% disp(H);
imshow(result);
saveas(gcf, "rectify.png");
