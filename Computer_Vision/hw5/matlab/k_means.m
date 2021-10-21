function Ikm = k_means(I,K) 

%% K-means Segmentation (option: K (Number of Clusters))
I = im2double(I);
F = reshape(I,size(I,1)*size(I,2),3);                 % Color Features
%% K-means
CENTS = F( ceil(rand(K,1)*size(F,1)) ,:);             % Cluster Centers
DAL   = zeros(size(F,1), K + 1);                         % Distances and Labels
KMI   = 10;                                           % K-means Iteration

for n = 1:KMI    
    for i = 1:size(F,1)
        % Iterate over each pixels to compute distance from cluster centers
        for j = 1:K
            DAL(i, j) = norm(CENTS(j) - F(i));
        end
        [m, idx] = min(DAL(i, :));
        DAL(i, K + 1) = idx(1);
    end
	for i = 1:K
        % Re-locate cluster centroids
        newCenter = zeros(1, 3);
        ptsCnt = 0;
        for j = 1:size(F, 1)
            if DAL(j, K + 1) == i
                newCenter = newCenter + F(j, :);
                ptsCnt = ptsCnt + 1;
            end
        end
        CENTS(i, :) = newCenter ./ ptsCnt;
	end
end

X = zeros(size(F));
for i = 1:K
	idx = find(DAL(:,K+1) == i);
    X(idx,:) = repmat(CENTS(i,:),size(idx,1),1); 
end
Ikm = reshape(X,size(I,1),size(I,2),3);             % Segmented image

end
