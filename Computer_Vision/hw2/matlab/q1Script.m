load('../data/bunny.mat');
nx = N(:, :, 1);
ny = N(:, :, 2);

figure(1);
quiver(nx, ny);
% q = quiver(nx, ny);
% saveas(q, 'q1quiver.png');

[sizeX, sizeY, sizeZ] = size(N);

rad = zeros(sizeX, sizeY);
for i = 1:sizeX
    for j = 1:sizeY
        rad(i, j) = dot(reshape(N(i, j, :), [1, 3]), [0 0 1]);
    end
end

figure(2);
imshow(rad);
% imwrite(rad, 'q1rad.png');
