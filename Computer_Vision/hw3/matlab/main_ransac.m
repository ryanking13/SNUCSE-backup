points = zeros(2, 200);

% 1 ~ 100: y = x
points(1, 1:100) = random('Uniform', -1, 1, [1, 100]);
points(2, 1:100) = points(1, 1:100);

% 101 ~ 200: random
points(:, 101:200) = random('Uniform', -1, 1, [2, 100]);

% add gaussian noise
points = points + random('Normal', 0, 0.1, [2, 200]);

iter = 6;
thr = 0.1;
mininlier = 10;
[line, inliers] = ransac(points, iter, thr, mininlier);

[sizeY, sizeX] = size(inliers);
color = zeros(sizeY, 3);
color(:, 1) = inliers(:);

figure
scatter(points(1, :), points(2, :), 10, color);
hold on;
plot(line(1, :), line(2, :));
hold off;
