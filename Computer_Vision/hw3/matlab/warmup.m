img1 = double(imread('../data/toys.jpg'))/255;
[sizeY, sizeX, sizeZ] = size(img1);

warped = zeros([sizeY * 2, sizeX, sizeZ]);

T = [1 0 0;
     0 2 0;
     0 0 1;];

mappingX = zeros(sizeY, sizeX);
mappingY = zeros(sizeY, sizeX);
for x = 1:sizeX
    for y = 1:(sizeY * 2)
        p = [x; y; 1];
        original = T \ p;
        mappingX(y, x) = original(1);
        mappingY(y, x) = original(2);
    end
end

warped(:, :, 1) = interp2(img1(:, :, 1), mappingX, mappingY);
warped(:, :, 2) = interp2(img1(:, :, 2), mappingX, mappingY);
warped(:, :, 3) = interp2(img1(:, :, 3), mappingX, mappingY);

imshow(warped);
