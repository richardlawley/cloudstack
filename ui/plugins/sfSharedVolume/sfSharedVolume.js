(function (cloudStack) {
  cloudStack.plugins.sfSharedVolume = function(plugin) {
    plugin.ui.addSection({
      id: 'sfSharedVolume',
      title: 'Shared Volume',
      preFilter: function(args) {
        return true;
      },
      listView: {
        id: 'sfSharedVolumes',
        fields: {
          name: { label: 'label.name' },
          iqn: { label: 'IQN' },
          size: { label: 'Size (GB)' },
          miniops: { label: 'Min IOPS' },
          maxiops: { label: 'Max IOPS' },
          burstiops: { label: 'Burst IOPS' }
        },
        dataProvider: function(args) {
          plugin.ui.apiCall('listSolidFireVolumes', {
            success: function(json) {
              var sfvolumes = json.listsolidfirevolumesresponse.sfvolume;

              args.response.success({ data: sfvolumes });
            },
            error: function(errorMessage) {
              args.response.error(errorMessage);
            }
          });
        },
        actions: {
          add: {
            label: 'Add Shared Volume',
            preFilter: function(args) {
              return true;
            },
            messages: {
              confirm: function(args) {
                return 'Please fill in the following data to add a new shared volume.';
              },
              notification: function(args) {
                return 'Add Shared Volume';
              }
            },
            createForm: {
              title: 'Add Shared Volume',
              desc: 'Please fill in the following data to add a new shared volume.',
              fields: {
                availabilityZone: {
                  label: 'label.availability.zone',
                  docID: 'helpVolumeAvailabilityZone',
                  validation: {
                    required: true
                  },
                  select: function(args) {
                    $.ajax({
                      url: createURL("listZones&available=true"),
                      dataType: "json",
                      async: true,
                      success: function(json) {
                        var zoneObjs = json.listzonesresponse.zone;

                        args.response.success({
                          descriptionField: 'name',
                          data: zoneObjs
                        });
                      }
                    });
                  }
                },
                name: {
                  label: 'label.name',
                  docID: 'helpVolumeName',
                  validation: {
                    required: true
                  }
                },
                diskSize: {
                  label: 'label.disk.size.gb',
                  validation: {
                    required: true,
                    number: true
                  }
                },
                minIops: {
                  label: 'label.disk.iops.min',
                  validation: {
                    required: true,
                    number: true
                  }
                },
                maxIops: {
                  label: 'label.disk.iops.max',
                  validation: {
                    required: true,
                    number: true
                  }
                },
                burstIops: {
                  label: 'Burst IOPS',
                  validation: {
                    required: true,
                    number: true
                  }
                },
                account: {
                  label: 'Account',
                  validation: {
                    required: true
                  },
                  isHidden: true,
                  select: function(args) {
                    if (isAdmin()) {
                      args.$form.find('.form-item[rel=account]').show();
                    }

                    $.ajax({
                      url: createURL("listAccounts"),
                      dataType: "json",
                      async: true,
                      success: function(json) {
                        var accountObjs = json.listaccountsresponse.account;

                        args.response.success({
                          descriptionField: 'name',
                          data: accountObjs
                        });
                      }
                    });
                  }
                },
                vlan: {
                  label: 'VLAN',
                  validation: {
                    required: true
                  },
                  select: function(args) {
                    $.ajax({
                      url: createURL("listSolidFireVirtualNetworks"),
                      dataType: "json",
                      async: true,
                      success: function(json) {
                        var virtualNetworkObjs = json.listsolidfirevirtualnetworksresponse.sfvirtualnetwork;

                        args.response.success({
                          descriptionField: 'name',
                          data: virtualNetworkObjs
                        });
                      }
                    });
                  }
                }
              }
            },
            action: function(args) {
              var data = {
                name: args.data.name,
                size: args.data.diskSize,
                miniops: args.data.minIops,
                maxiops: args.data.maxIops,
                burstiops: args.data.burstIops,
                accountid: args.data.account,
                sfvirtualnetworkid: args.data.vlan
              };

              $.ajax({
                url: createURL('createSolidFireVolume'),
                data: data,
                success: function(json) {
                  var sfvolumeObj = json.createsolidfirevolumeresponse.apicreatesolidfirevolume;

                  args.response.success({
                    data: sfvolumeObj
                  });
                },
                error: function(json) {
                  args.response.error(parseXMLHttpResponse(json));
                }
              });
            }
          }
        },
        detailView: {
          name: 'Shared volume details',
          isMaximized: true,
          actions: {
            edit: {
              label: 'Edit shared volume',
              compactLabel: 'label.edit',
              action: function(args) {
                var sharedVolumeObj = args.context;
              }
            }
          }
        }
      }
    });
  };
}(cloudStack));