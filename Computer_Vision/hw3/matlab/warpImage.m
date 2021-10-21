function [Iwarp, Imerge] = warpImage(Iin, Iref, H)
    [sizeY, sizeX, sizeZ] = size(Iref);
    warped = zeros([sizeY, sizeX, sizeZ]);
    
    mappingX = zeros(sizeY, sizeX);
    mappingY = zeros(sizeY, sizeX);

    for x = 1:sizeX
        for y = 1:sizeY
            m = H * [x; y; 1];
            mappingX(y, x) = m(1) / m(3);
            mappingY(y, x) = m(2) / m(3);
        end
    end
    
    warped(:, :, 1) = interp2(Iin(:, :, 1), mappingX, mappingY);
    warped(:, :, 2) = interp2(Iin(:, :, 2), mappingX, mappingY);
    warped(:, :, 3) = interp2(Iin(:, :, 3), mappingX, mappingY);
    
    % warped image
    Iwarp = imfuse(Iref, warped, 'blend');
    
    % padding for merged image
    lpadX = fix(sizeX * 0.5);
    lpadY = fix(sizeY * 0.3);
    msizeX = sizeX + lpadX;
    msizeY = sizeY + lpadY;

    merged = zeros([msizeY, msizeX, sizeZ]);
    
    mappingX = zeros(msizeY, msizeX);
    mappingY = zeros(msizeY, msizeX);

    for x = 1:msizeX
        for y = 1:msizeY
            mx = x - lpadX;
            my = y - lpadY;
            m = H * [mx; my; 1];
            mappingX(y, x) = m(1) / m(3);
            mappingY(y, x) = m(2) / m(3);
        end
    end
    
    merged(:, :, 1) = interp2(Iin(:, :, 1), mappingX, mappingY);
    merged(:, :, 2) = interp2(Iin(:, :, 2), mappingX, mappingY);
    merged(:, :, 3) = interp2(Iin(:, :, 3), mappingX, mappingY);
    
    Imerge = merged;

    for x = 1:sizeX
        for y = 1:sizeY
            Imerge(y + lpadY, x + lpadX, :) = Iref(y, x, :);
        end
    end
    
end
