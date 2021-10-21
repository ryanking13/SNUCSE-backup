function [normals, albedo] = computeNormals(img1, img2, img3, lv1, lv2, lv3, threshold)
    [imgY, imgX] = size(img1);
    N = imgY * imgX;
    
    S = [lv1; lv2; lv3];
    img1v = reshape(img1, [1, N]);
    img2v = reshape(img2, [1, N]);
    img3v = reshape(img3, [1, N]);
    I = [img1v; img2v; img3v;];
    tildeN = S \ I;
    albedo = zeros(1, N);
    
    for i = 1:N
        albedo(i) = norm(tildeN(:, i));
    end
    normals = tildeN ./ albedo;
    
    for i = 1:N
        if img1v(i) < threshold || img2v(i) < threshold || img3v(i) < threshold
            normals(:, i) = [0, 0, 0];
            albedo(i) = 0;
        end
    end
    
    albedo = reshape(albedo, size(img1));
end
