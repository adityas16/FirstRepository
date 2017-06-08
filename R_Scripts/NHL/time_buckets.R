create_bucket_index = function(bucket_boundaries = c(5,10,15,20,25,30,35,40,45,50,58,60)){
  n_buckets = length(bucket_boundaries)
  bucket_index=rep(NA,bucket_boundaries[n_buckets])
  bucket_index[1] = 1
  bucket_boundaries = bucket_boundaries[1:n_buckets-1]
  bucket_index[bucket_boundaries+1] = 2:n_buckets
  bucket_index = na.locf(bucket_index)
  return(bucket_index)
}
bucket_index = create_bucket_index()

