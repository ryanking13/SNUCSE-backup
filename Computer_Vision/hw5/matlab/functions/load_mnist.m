function [train, test] = load_mnist(binary_digits, standardize)

  if nargin < 1
    binary_digits = false;
  end
  if nargin < 2
    standardize = false;
  end

  % Load the training data
  X=loadMNISTImages('train-images-idx3-ubyte');
  y=loadMNISTLabels('train-labels-idx1-ubyte')';

  if (binary_digits)
    % Take only the 0 and 1 digits
    X = [ X(:,y==0), X(:,y==1) ];
    y = [ y(y==0), y(y==1) ];
  end

  % Randomly shuffle the data
  I = randperm(length(y));
  y=y(I); % labels in range 1 to 10
  X=X(:,I);

  s=std(X,[],2);
  m=mean(X,2);
  
  % standardize the data so that each pixel will have roughly zero mean and unit variance.
  if(standardize)
      X=bsxfun(@minus, X, m);
      X=bsxfun(@rdivide, X, s+.1);
  end

  % Place these in the training set
  train.X = X;
  train.y = y;

  % Load the testing data
  X=loadMNISTImages('t10k-images-idx3-ubyte');
  y=loadMNISTLabels('t10k-labels-idx1-ubyte')';

  if (binary_digits)
    % Take only the 0 and 1 digits
    X = [ X(:,y==0), X(:,y==1) ];
    y = [ y(y==0), y(y==1) ];
  end

  % Randomly shuffle the data
  I = randperm(length(y));
  y=y(I); % labels in range 1 to 10
  X=X(:,I);

  % Standardize using the same mean and scale as the training data.
  if(standardize)
      X=bsxfun(@minus, X, m);
      X=bsxfun(@rdivide, X, s+.1);
  end

  % Place these in the testing set
  test.X=X;
  test.y=y;

