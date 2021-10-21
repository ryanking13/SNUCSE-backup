function lv = findLight(img, cx, cy, r)
    [imgY, imgX] = size(img);
    rSqr = r * r;
    
    maxIntensity = -1;
    for y = 1:imgY
        for x = 1:imgX
            if (y - cy) ^ 2 + (x - cx) ^ 2 > rSqr % outside of circle boundary
                continue
            end
            if img(y, x) > maxIntensity
                maxIntensity = img(y, x);
                maxX = x;
                maxY = y;
            end
        end
    end
    
    dy = maxY - cy;
    dx = maxX - cx;
%     dz = (r^2 - dy^2) + (r^2 - dx^2) / 2;
    dz = sqrt(r^2 - (dx^2 + dy^2));
%     d = sqrt(dy^2 + dx^2 + dz^2);
    lv = maxIntensity * [dx, dy, dz] / r;
end
