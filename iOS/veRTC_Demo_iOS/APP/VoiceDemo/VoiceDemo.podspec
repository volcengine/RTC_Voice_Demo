
Pod::Spec.new do |spec|
  spec.name         = 'VoiceDemo'
  spec.version      = '1.0.0'
  spec.summary      = 'VoiceDemo APP'
  spec.description  = 'VoiceDemo App Demo..'
  spec.homepage     = 'https://github.com/volcengine'
  spec.license      = { :type => 'MIT', :file => 'LICENSE' }
  spec.author       = { 'author' => 'volcengine rtc' }
  spec.source       = { :path => './'}
  spec.ios.deployment_target = '9.0'
  
  spec.source_files = '**/*.{h,m,c,mm}'
  spec.resource_bundles = {
    'VoiceDemo' => ['Resource/*.xcassets']
  }
  spec.prefix_header_contents = '#import "Masonry.h"',
                                '#import "Core.h"',
                                '#import "VoiceRTCManager.h"',
                                '#import "VoiceDemoConstants.h"'
  spec.dependency 'Core'
  spec.dependency 'YYModel'
  spec.dependency 'Masonry'
  spec.dependency 'VolcEngineRTC'
end
