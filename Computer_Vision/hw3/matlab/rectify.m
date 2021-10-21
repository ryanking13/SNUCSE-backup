function [H, result] = rectify(img, p1, p2)
    H = computeHnorm(p1, p2);
    
    [sizeY, sizeX, sizeZ] = size(img);
    result = zeros([sizeY, sizeX, sizeZ]);
    
    mappingX = zeros(sizeY, sizeX);
    mappingY = zeros(sizeY, sizeX);

    for x = 1:sizeX
        for y = 1:sizeY
            m = H * [x; y; 1];
            mappingX(y, x) = m(1) / m(3);
            mappingY(y, x) = m(2) / m(3);
        end
    end
    
    result(:, :, 1) = interp2(img(:, :, 1), mappingX, mappingY);
    result(:, :, 2) = interp2(img(:, :, 2), mappingX, mappingY);
    result(:, :, 3) = interp2(img(:, :, 3), mappingX, mappingY);
end
